package uk.gov.example.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.example.baseline.HeaderOptions;
import uk.gov.example.baseline.Policy;
import uk.gov.example.baseline.ResponseHeaders;
import uk.gov.example.baseline.ResponseKind;

@Controller
public class AssetController {

  private final AssetRegistry assets;
  private final Policy policy;

  public AssetController(AssetRegistry assets, Policy policy) {
    this.assets = assets;
    this.policy = policy;
  }

  @GetMapping("/assets/{*resourcePath}")
  public ResponseEntity<byte[]> asset(@PathVariable String resourcePath) {
    String path =
        "/assets/" + (resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath);
    Optional<AssetRegistry.Asset> resolved = assets.resolve(path);
    if (resolved.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    AssetRegistry.Asset asset = resolved.get();
    Map<String, String> headerMap = new LinkedHashMap<>();
    ResponseKind kind =
        asset.fingerprinted() ? ResponseKind.FINGERPRINTED_ASSET : ResponseKind.STATIC_ASSET;
    ResponseHeaders.applyHeaders(policy, headerMap, new HeaderOptions(kind, false, false));
    HttpHeaders headers = new HttpHeaders();
    headerMap.forEach(headers::set);
    headers.set(HttpHeaders.CONTENT_TYPE, asset.contentType());
    return new ResponseEntity<>(asset.body(), headers, HttpStatus.OK);
  }
}
