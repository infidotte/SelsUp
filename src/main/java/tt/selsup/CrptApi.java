package tt.selsup;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Data

public class CrptApi{
    private final String baseURL;
    private final String productGroup;
    private final String token;
    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private long interval;
    private int requestLimit;

    private int requestCounter;
    private long lastTime;

    public CrptApi(String baseURL, String productGroup, String token, TimeUnit timeUnit, int requestLimit, int requestCounter, long lastTime){
        this.baseURL = baseURL;
        this.productGroup = productGroup;
        this.token = token;
        this.interval = timeUnit.toMillis(1);
        this.requestLimit = requestLimit;
        this.requestCounter = requestCounter;
        this.lastTime = lastTime;
    }

    private static String getFormDataAsString(Map<String, String> formData) {
        StringBuilder formBodyBuilder = new StringBuilder();
        for (Map.Entry<String, String> singleEntry : formData.entrySet()) {
            if (formBodyBuilder.length() > 0) {
                formBodyBuilder.append("&");
            }
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
            formBodyBuilder.append("=");
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
        }
        return formBodyBuilder.toString();
    }
    private String save(Document document, String signature) throws InterruptedException {

        long current = System.currentTimeMillis();
        if(current - lastTime < interval){
            if(requestCounter < requestLimit){
                requestCounter++;
                lastTime = current;
                executorService.execute(()-> saveDocument(document, signature));
            }else {
                requestCounter = 0;
                lastTime = current;
                Thread.sleep(interval);
            }

        }else{
            requestCounter = 0;
            lastTime = current;
            Thread.sleep(interval);
        }
        return null;
    }

    private String saveDocument(Document document, String signature) {
        Gson gson = new Gson();
        String serialized = gson.toJson(document);
        String URL = baseURL + "/api/v3/lk/documents/create";
        Map<String, String> body = new HashMap<>();
        body.put("document_format", "MANUAL");
        body.put("product_document", serialized);
        body.put("product_group", productGroup);
        body.put("signature", signature);
        body.put("type", "LP_INTRODUCE_GOODS");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(getFormDataAsString(body)))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode == 200) {
                Response resp = gson.fromJson(response.body(), Response.class);
                return resp.toString();
            } else {
                Error error = gson.fromJson(response.body(), Error.class);
                return error.toString();
            }
        } catch (Exception e) {
            return null;
        }
    }

}

@Data
class Document {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Description description;
    private String doc_id;
    private String doc_status;
    private String doc_type;
    private boolean importRequest;
    private String owner_inn;
    private String participant_inn;
    private String producer_inn;
    private String production_date;
    private String production_type;
    private List<Product> products;
    private String reg_date;
    private String reg_number;

    public Document(Description description, String doc_id, String doc_status, String doc_type, boolean importRequest, String owner_inn, String participant_inn, String producer_inn, Date production_date, String production_type, List<Product> products, Date reg_date, String reg_number) {
        this.description = description;
        this.doc_id = doc_id;
        this.doc_status = doc_status;
        this.doc_type = doc_type;
        this.importRequest = importRequest;
        this.owner_inn = owner_inn;
        this.participant_inn = participant_inn;
        this.producer_inn = producer_inn;
        this.production_date = simpleDateFormat.format(production_date);
        this.production_type = production_type;
        this.products = products;
        this.reg_date = simpleDateFormat.format(reg_date);
        this.reg_number = reg_number;
    }

    @Data
    @AllArgsConstructor
    class Description {
        private String participantInn;
    }

    @Data
    class Product {
        private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;

        public Product(String certificate_document, Date certificate_document_date, String certificate_document_number, String owner_inn, String producer_inn, Date production_date, String tnved_code, String uit_code, String uitu_code) {
            this.certificate_document = certificate_document;
            this.certificate_document_date = simpleDateFormat.format(certificate_document_date);
            this.certificate_document_number = certificate_document_number;
            this.owner_inn = owner_inn;
            this.producer_inn = producer_inn;
            this.production_date = simpleDateFormat.format(production_date);
            this.tnved_code = tnved_code;
            this.uit_code = uit_code;
            this.uitu_code = uitu_code;
        }

    }
}

@Data
@AllArgsConstructor
@ToString
class Response {
    private String value;
}

@Data
@AllArgsConstructor
@ToString
class Error {
    private String code;
    private String error_massage;
    private String description;
}


