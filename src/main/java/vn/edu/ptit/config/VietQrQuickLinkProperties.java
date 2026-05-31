package vn.edu.ptit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vietqr.quicklink")
public class VietQrQuickLinkProperties {
    private String bankId;
    private String accountNo;
    private String template = "compact2";
    private String accountName;
}
