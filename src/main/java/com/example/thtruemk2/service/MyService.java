package com.example.thtruemk2.service;

import com.example.thtruemk2.model.threquest;
import com.example.thtruemk2.model.thresponse1;
import com.fasterxml.jackson.databind.JsonNode;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class MyService {

    private WebClient webClient;
    private threquest request;
    public List<String> cookies;
    public String requestVerificationToken;
    private final List<String[]> proxyList = new ArrayList<>();
    AtomicInteger proxyIndex = new AtomicInteger(0);
    AtomicInteger currentProxyIndex = new AtomicInteger(0);
    private static final int REQUEST_LIMIT = 100;
    @Autowired
    public MyService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://quatangtopkid.thmilk.vn")
                .defaultHeader(HttpHeaders.USER_AGENT,"Mozilla/5.0 (Linux; Android 9; SM-G977N Build/PQ3A.190605.003; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/91.0.4472.114 Mobile Safari/537.36")
                .build();
        ResponseEntity<String> response = webClient.get()
                .uri("/")
                .retrieve()
                .toEntity(String.class)
                .block();
        cookies = response.getHeaders().get("Set-Cookie");
        String htmlContent = response.getBody();
        Document document = Jsoup.parse(htmlContent);
        Element tokenInput = document.selectFirst("input[name=__RequestVerificationToken]");
        requestVerificationToken = tokenInput.attr("value");
    }
//    @PostConstruct
//    private void init() {
//        loadProxies("fileproxy.txt");
//        configureWebClient();
//    }
    public void configureWebClient2(String proxy) {
        try {
            // Tách proxy thành các phần: host, port, username, password
            String[] parts = proxy.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            // Cấu hình HttpClient với proxy
            HttpClient httpClient = HttpClient.create()
                    .proxy(proxyOptions -> proxyOptions.type(ProxyProvider.Proxy.HTTP)
                            .address(new InetSocketAddress(host, port))
                    );

            // Tạo WebClient với HttpClient đã cấu hình
            webClient = WebClient.builder()
                    .baseUrl("https://quatangtopkid.thmilk.vn")
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();

            log.info("WebClient đã được cấu hình với proxy: {}", proxy);
        } catch (Exception e) {
            log.error("Lỗi khi cấu hình proxy: {}", e.getMessage(), e);
        }
    }

    private void loadProxies(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                proxyList.add(line.split(":"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void configureWebClient() {
        String[] currentProxy = proxyList.get(currentProxyIndex.get());
        HttpClient httpClient = HttpClient.create()
//                .secure(sslContextSpec -> {
//                    try {
//                        sslContextSpec.sslContext(SslContextBuilder.forClient().build()); // Dùng cấu hình SSL mặc định
//                    } catch (SSLException e) {
//                        throw new RuntimeException(e);
//                    }
//                })
                .proxy(proxy -> proxy.type(ProxyProvider.Proxy.HTTP)
                        .address(new InetSocketAddress(currentProxy[0], Integer.parseInt(currentProxy[1])))
                        .username(currentProxy[2])
                        .password(pass -> currentProxy[3]));

        webClient = WebClient.builder()
                .baseUrl("https://quatangtopkid.thmilk.vn")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
    public void switchProxyIfNeeded() {
        if (currentProxyIndex.get() < proxyList.size() - 1) {
            currentProxyIndex.incrementAndGet();
        } else {
            currentProxyIndex.set(0); // Quay lại proxy đầu tiên
        }
        configureWebClient();
    }

    public String sendPostRequest(threquest requestObject) throws InterruptedException, IOException {
        request= requestObject;
        String value = "Name=NGUYEN+HUU+QUYNH"+ "&" + "Phone=0" + requestObject.getPhone()+"&ProvinceCode=01"+"&Code=" + requestObject.getCode() ;
        String firstCookie = cookies.get(0).split(";")[0];

        try {

            String response = webClient.post()
                    .uri("/Home/IndexAjax")
                    .header("Host", "quatangtopkid.thmilk.vn")
                    .header("requestverificationtoken",requestVerificationToken)
                    .header("accept", "*/*")
                    .header("x-requested-with", "XMLHttpRequest")
                    .header("user-agent", "Mozilla/5.0 (Linux; Android 9; SM-G977N Build/PQ3A.190605.003; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/91.0.4472.114 Mobile Safari/537.36")
                    .header("origin", "https://quatangtopkid.thmilk.vn")
                    .header("sec-fetch-site", "same-origin")
                    .header("sec-fetch-mode", "cors")
                    .header("sec-fetch-dest", "empty")
                    .header("referer","https://quatangtopkid.thmilk.vn/")
                    //.header("accept-encoding","gzip, deflate")
                    .header("Cookie", firstCookie)
                    .header("accept-language", "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .bodyValue(value)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(10))
                    .map(jsonNode -> {
                        JsonNode Type = jsonNode.get("Prize");
                        return (Type != null) ? Type.asText() : "";
                    })
                    .block();
            log.info(response);
//            if(response.isEmpty()){
//                ResponseEntity<String> response1 = webClient.get()
//                        .uri("/")
//                        .retrieve()
//                        .toEntity(String.class)
//                        .block();
//                cookies = response1.getHeaders().get("Set-Cookie");
//                String htmlContent = response1.getBody();
//                Document document = Jsoup.parse(htmlContent);
//                Element tokenInput = document.selectFirst("input[name=__RequestVerificationToken]");
//                requestVerificationToken = tokenInput.attr("value");
//            }
            return response;
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException occurred: Status Code = {}, Response Body = {}",
                    e.getRawStatusCode(), e.getResponseBodyAsString());
        }
        catch (Exception e) {
            // Bắt các ngoại lệ khác
            log.error("An unexpected error occurred: ", e);
        }

        return "";
    }
    public String updatewinner(String token, String cccd) throws InterruptedException {
        String value="WinnerInfo.Name="+request.getName()+"&WinnerInfo.Code="+request.getCode()+"&WinnerInfo.Phone="+request.getPhone()
                +"&WinnerInfo.NationalId="+cccd
                +"&WinnerInfo.Province=01&WinnerInfo.District=005&WinnerInfo.Store=STORE145&__RequestVerificationToken="+token;
        log.info(value);
        try {

            String response = webClient.post()
                    .uri("/Home/UpdateWinnerInfo")
                    .header("Host", "quatangtopkid.thmilk.vn")
                    .header("cache-control", "max-age=0")
                    .header("upgrade-insecure-requests","1")
                    .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("x-requested-with", "com.android.browser")
                    .header("user-agent", "Mozilla/5.0 (Linux; Android 9; SM-G977N Build/PQ3A.190605.003; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/91.0.4472.114 Mobile Safari/537.36")
                    .header("origin", "https://quatangtopkid.thmilk.vn")
                    .header("sec-fetch-site", "same-origin")
                    .header("sec-fetch-mode", "navigate")
                    .header("sec-fetch-dest", "document")
                    .header("sec-fetch-user","?1")
                    .header("referer","https://quatangtopkid.thmilk.vn")
                    .header("accept-encoding","gzip, deflate")
                    .header("accept-language", "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("content-type", "application/x-www-form-urlencoded")
                    .bodyValue(value)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return "thanhcong";
        } catch (WebClientResponseException e) {
            return "khong thanh cong";
        }
    }

}
