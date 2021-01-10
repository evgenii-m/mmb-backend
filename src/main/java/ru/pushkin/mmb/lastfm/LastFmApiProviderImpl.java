package ru.pushkin.mmb.lastfm;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import ru.pushkin.mmb.config.ServicePropertyConfig;
import ru.pushkin.mmb.lastfm.model.*;
import ru.pushkin.mmb.utils.StreamUtils;
import ru.pushkin.mmb.utils.XmlUtils;

import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * WARNING: this provider is not thread safe
 */
@Slf4j
@Component
public class LastFmApiProviderImpl implements LastFmApiProvider {

    private static final String LASTFM_API_SCHEME = "https";
    private static final String LASTFM_API_HOST = "ws.audioscrobbler.com/";
    private static final String LASTFM_API_VERSION = "2.0/";
    private static final String LASTFM_RESPONSE_STATUS_OK = "ok";

    private final CloseableHttpClient httpClient;
    private final ServicePropertyConfig servicePropertyConfig;

    public LastFmApiProviderImpl(ServicePropertyConfig servicePropertyConfig) {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(20);

        int timeout = 5;
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();

        this.servicePropertyConfig = servicePropertyConfig;
        this.httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    /**
     * See https://www.last.fm/api/show/auth.getToken
     */
    @Override
    public String authGetToken() {
        XPathFactory xPathFactory = XPathFactory.newInstance();

        LastFmApiMethod method = LastFmApiMethod.AUTH_GET_TOKEN;
        Map<String, String> methodParameters = new HashMap<>();

        try {
            String response = makeApiGetRequest(method, methodParameters);
            if (response != null) {
                Document responseDocument = XmlUtils.convertStringToXmlDocument(response);

                String expression = ".//*[local-name() = 'token']";
                String token = (String) xPathFactory.newXPath().evaluate(expression, responseDocument, XPathConstants.STRING);
                log.info("API token obtained: " + token);
                return token;
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            log.error("parsing xml from response error: ", e);
        } catch (XPathExpressionException e) {
            log.error("extract element from xml response error: ", e);
        }

        return null;
    }

    /**
     * See https://www.last.fm/api/show/auth.getSession
     */
    @Override
    public Optional<String> authGetSession(String token) {
        XPathFactory xPathFactory = XPathFactory.newInstance();

        LastFmApiMethod method = LastFmApiMethod.AUTH_GET_SESSION;
        Map<String, String> methodParameters = new HashMap<>();
        methodParameters.put(LastFmApiParam.TOKEN.getName(), token);

        try {
            String response = makeApiGetRequest(method, methodParameters, servicePropertyConfig.getLastFm().getSessionRetryCount());
            if (response != null) {
                Document responseDocument = XmlUtils.convertStringToXmlDocument(response);

                String expression = ".//*[local-name() = 'name']";
                String username = (String) xPathFactory.newXPath().evaluate(expression, responseDocument, XPathConstants.STRING);
                log.info("session username: " + username);

                expression = ".//*[local-name() = 'key']";
                String sessionKey = (String) xPathFactory.newXPath().evaluate(expression, responseDocument, XPathConstants.STRING);
                log.info("session key: " + sessionKey);

                return Optional.of(sessionKey);
            }
        } catch (IOException| SAXException | ParserConfigurationException e) {
            log.error("parsing xml from response error: ", e);
        } catch (XPathExpressionException e) {
            log.error("extract element from xml response error: ", e);
        }

        return Optional.empty();
    }

    /**
     * See https://www.last.fm/api/show/user.getRecentTracks
     */
    @Override
    public Optional<RecentTracks> userGetRecentTracks(@NotNull String username, Integer page, Integer limit,
                                                      Date from, Date to, Boolean extended) {
        LastFmApiMethod method = LastFmApiMethod.USER_GET_RECENT_TRACKS;
        Map<String, String> methodParameters = new HashMap<>();
        methodParameters.put(LastFmApiParam.USER.getName(), username);
        setPageAndLimitParameters(methodParameters, page, limit);
        if (from != null) {
            methodParameters.put(LastFmApiParam.FROM.getName(), String.valueOf(from.getTime() / 1000));
        }
        if (to != null) {
            methodParameters.put(LastFmApiParam.TO.getName(), String.valueOf(to.getTime() / 1000));
        }
        if (extended != null) {
            methodParameters.put(LastFmApiParam.EXTENDED.getName(), extended ? "1" : "0");
        }

        try {
            String response = makeApiRequest(method, methodParameters, 3, false, HttpGet::new);
            if (response != null) {
                LastFmResponse lastFmResponse = XmlUtils.unmarshalDocumnet(response, LastFmResponse.class.getPackage().getName());
                if (validateLastFmResponse(lastFmResponse)) {
                    RecentTracks recentTracks = lastFmResponse.getRecentTracks();
                    log.debug("obtained recent tracks: {}", recentTracks);
                    return Optional.ofNullable(recentTracks);
                }
            }
            log.warn("empty response: {}", response);
        } catch (JAXBException e) {
            log.error("parsing xml from response error: ", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<TrackInfo> trackGetInfo(String mbid, String track, String artist, String username, Boolean autocorrect) {
        LastFmApiMethod method = LastFmApiMethod.TRACK_GET_INFO;
        Map<String, String> methodParameters = new HashMap<>();
        if (mbid != null) {
            methodParameters.put(LastFmApiParam.MBID.getName(), mbid);
        }
        if (track != null) {
            methodParameters.put(LastFmApiParam.TRACK.getName(), track);
        }
        if (artist != null) {
            methodParameters.put(LastFmApiParam.ARTIST.getName(), artist);
        }
        if (username != null) {
            methodParameters.put(LastFmApiParam.USERNAME.getName(), username);
        }
        if (autocorrect != null) {
            methodParameters.put(LastFmApiParam.AUTOCORRECT.getName(), autocorrect ? "1" : "0");
        }

        try {
            String response = makeApiGetRequest(method, methodParameters);
            if (response != null) {
                LastFmResponse lastFmResponse = XmlUtils.unmarshalDocumnet(response, LastFmResponse.class.getPackage().getName());
                if (validateLastFmResponse(lastFmResponse)) {
                    TrackInfo trackInfo = lastFmResponse.getTrackInfo();
                    log.debug("obtained track info: {}", trackInfo);
                    return Optional.ofNullable(trackInfo);
                }
            }
        } catch (JAXBException e) {
            log.error("parsing xml from response error: ", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<UpdateNowPlayingResult> updateNowPlaying(@NotNull String sessionKey, @NotNull String artist,
                                                             @NotNull String track, String album, Long duration) {
        LastFmApiMethod method = LastFmApiMethod.TRACK_UPDATE_NOW_PLAYING;
        Map<String, String> methodParameters = new HashMap<>();
        methodParameters.put(LastFmApiParam.SESSION_KEY.getName(), sessionKey);
        methodParameters.put(LastFmApiParam.ARTIST.getName(), artist);
        methodParameters.put(LastFmApiParam.TRACK.getName(), track);
        if (album != null) {
            methodParameters.put(LastFmApiParam.ALBUM.getName(), album);
        }
        if (duration != null) {
            methodParameters.put(LastFmApiParam.DURATION.getName(), String.valueOf(duration));
        }

        try {
            String response = makeApiPostRequest(method, methodParameters);
            if (response != null) {
                LastFmResponse lastFmResponse = XmlUtils.unmarshalDocumnet(response, LastFmResponse.class.getPackage().getName());
                if (validateLastFmResponse(lastFmResponse)) {
                    UpdateNowPlayingResult updateNowPlayingResult = lastFmResponse.getUpdateNowPlayingResult();
                    log.debug("obtained updating now playing result: {}", updateNowPlayingResult);
                    return Optional.ofNullable(updateNowPlayingResult);
                }
            }
        } catch (JAXBException e) {
            log.error("parsing xml from response error: ", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<ScrobblesResult> scrobbleTrack(@NotNull String sessionKey, @NotNull String artist, @NotNull String track,
                                                   int timestamp, String album, Boolean chosenByUser, Long duration) {
        LastFmApiMethod method = LastFmApiMethod.TRACK_SCROBBLE;
        Map<String, String> methodParameters = new HashMap<>();
        methodParameters.put(LastFmApiParam.SESSION_KEY.getName(), sessionKey);
        methodParameters.put(LastFmApiParam.ARTIST.getName(), artist);
        methodParameters.put(LastFmApiParam.TRACK.getName(), track);
        methodParameters.put(LastFmApiParam.TIMESTAMP.getName(), String.valueOf(timestamp));
        if (album != null) {
            methodParameters.put(LastFmApiParam.ALBUM.getName(), album);
        }
        if (chosenByUser != null) {
            methodParameters.put(LastFmApiParam.CHOSEN_BY_USER.getName(), chosenByUser ? "1" : "0");
        }
        if (duration != null) {
            methodParameters.put(LastFmApiParam.DURATION.getName(), String.valueOf(duration));
        }

        try {
            String response = makeApiPostRequest(method, methodParameters);
            if (response != null) {
                LastFmResponse lastFmResponse = XmlUtils.unmarshalDocumnet(response, LastFmResponse.class.getPackage().getName());
                if (validateLastFmResponse(lastFmResponse)) {
                    ScrobblesResult scrobblesResult = lastFmResponse.getScrobblesResult();
                    log.debug("obtained scrobbles result: {}", scrobblesResult);
                    return Optional.ofNullable(scrobblesResult);
                }
            }
        } catch (JAXBException e) {
            log.error("parsing xml from response error: ", e);
        }

        return Optional.empty();
    }

    /**
     * See https://www.last.fm/api/show/user.getInfo
     */
    @Override
    public Optional<User> userGetInfo(String user) {
        LastFmApiMethod method = LastFmApiMethod.USER_GET_INFO;
        Map<String, String> methodParameters = new HashMap<>();

        methodParameters.put(LastFmApiParam.USER.getName(), user);

        try {
            String response = makeApiRequest(method, methodParameters, 0, false, HttpGet::new);
            if (response != null) {
                LastFmResponse lastFmResponse = XmlUtils.unmarshalDocumnet(response, LastFmResponse.class.getPackage().getName());
                if (validateLastFmResponse(lastFmResponse)) {
                    User responseUser = lastFmResponse.getUser();
                    log.debug("obtained user: {}", responseUser);
                    return Optional.ofNullable(responseUser);
                }
            }
        } catch (JAXBException e) {
            log.error("parsing xml from response error: ", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<LovedTracks> userGetLovedTracks(@NotNull String username, Integer page, Integer limit) {
        LastFmApiMethod method = LastFmApiMethod.USER_GET_LOVED_TRACKS;
        Map<String, String> methodParameters = new HashMap<>();
        methodParameters.put(LastFmApiParam.USER.getName(), username);
        setPageAndLimitParameters(methodParameters, page, limit);

        try {
            String response = makeApiRequest(method, methodParameters, 0, false, HttpGet::new);
            if (response != null) {
                LastFmResponse lastFmResponse = XmlUtils.unmarshalDocumnet(response, LastFmResponse.class.getPackage().getName());
                if (validateLastFmResponse(lastFmResponse)) {
                    LovedTracks lovedTracks = lastFmResponse.getLovedTracks();
                    log.debug("obtained loved tracks: {}", lovedTracks);
                    return Optional.ofNullable(lovedTracks);
                }
            }
        } catch (JAXBException e) {
            log.error("parsing xml from response error: ", e);
        }

        return Optional.empty();
    }

    private static void setPageAndLimitParameters(Map<String, String> methodParameters, Integer page, Integer limit) {
        if (limit != null) {
            methodParameters.put(LastFmApiParam.LIMIT.getName(), String.valueOf(limit));
        }
        if (page != null) {
            methodParameters.put(LastFmApiParam.PAGE.getName(), String.valueOf(page + 1));
        }
    }

    private String makeApiGetRequest(LastFmApiMethod method, Map<String, String> methodParameters) {
        return makeApiRequest(method, methodParameters, 0, true, HttpGet::new);
    }

    private String makeApiGetRequest(LastFmApiMethod method, Map<String, String> methodParameters, int retryCount) {
        return makeApiRequest(method, methodParameters, retryCount, true, HttpGet::new);
    }

    private String makeApiPostRequest(LastFmApiMethod method, Map<String, String> methodParameters) {
        return makeApiRequest(method, methodParameters, 0, true, HttpPost::new);
    }

    private String makeApiPostRequest(LastFmApiMethod method, Map<String, String> methodParameters, int retryCount) {
        return makeApiRequest(method, methodParameters, retryCount, true, HttpPost::new);
    }

    private <T extends HttpRequestBase> String makeApiRequest(LastFmApiMethod method, Map<String, String> methodParameters,
                                                              int retryCount, boolean setSignature, Function<URI, T> constructRequestFunction) {
        URIBuilder baseApiUriBuilder = new URIBuilder()
                .setScheme(LASTFM_API_SCHEME)
                .setHost(LASTFM_API_HOST)
                .setPath(LASTFM_API_VERSION);

        // append parameters required for all methods
        methodParameters.put(LastFmApiParam.METHOD_NAME.getName(), method.getName());
        methodParameters.put(LastFmApiParam.API_KEY.getName(), servicePropertyConfig.getLastFm().getApplicationApiKey());

        // calculate signature for method parameters
        if (setSignature) {
            String methodSignature = getApiMethodSignature(methodParameters);
            methodParameters.put(LastFmApiParam.API_SIG.getName(), methodSignature);
        }

        // make request
        baseApiUriBuilder.clearParameters();
        methodParameters.forEach(baseApiUriBuilder::addParameter);

        T request = null;
        try {
            URI apiUri = baseApiUriBuilder.build();

            // TODO: make request in other thread, not to delay UI
            for (; retryCount >= 0; retryCount--, Thread.sleep(servicePropertyConfig.getLastFm().getRetryTimeoutSec() * 1000)) {
                request = constructRequestFunction.apply(apiUri);
                try {
                    log.debug("api request: {}", request);
                    try (CloseableHttpResponse response = httpClient.execute(request)) {
                        log.debug("api response: {}", response);
                        // process response
                        int statusCode = response.getStatusLine().getStatusCode();
                        if (HttpStatus.SC_OK == statusCode) {
                            String responseContent = StreamUtils.readStreamAsOneString(response.getEntity().getContent());
                            log.debug("response content: {}", responseContent);
                            return responseContent;
                        }
                    }
                } finally {
                    request.releaseConnection();
                }
            }

        } catch (URISyntaxException e) {
            log.error("construct uri error: ", e);
        } catch (IOException e) {
            log.error("http request error: request = {}, exception = {}", request, e);
        } catch (InterruptedException e) {
            log.error("timeout error: request = {}, exception = {}", request, e);
        }

        return null;
    }

    /**
     * Signature calculated according to https://www.last.fm/api/desktopauth#6
     */
    private String getApiMethodSignature(Map<String, String> parameters) {
        String signatureString = parameters.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> e.getKey() + e.getValue())
                .collect(Collectors.joining());

        signatureString += servicePropertyConfig.getLastFm().getApplicationApiSharedSecret();

        return DigestUtils.md5Hex(signatureString);
    }

    private boolean validateLastFmResponse(LastFmResponse lastFmResponse) {
        if ((lastFmResponse != null) && (LASTFM_RESPONSE_STATUS_OK.equals(lastFmResponse.getStatus()))) {
            return true;
        } else {
            log.error("Invalid LastFmResponse status: response = {}", lastFmResponse);
            return false;
        }
    }
}
