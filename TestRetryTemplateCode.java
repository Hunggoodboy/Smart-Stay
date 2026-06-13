import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import java.util.Collections;

public class TestRetryTemplateCode {
    public static void main(String[] args) {
        RetryTemplate template = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(25000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(100000);
        template.setBackOffPolicy(backOffPolicy);
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, Collections.singletonMap(Exception.class, true));
        template.setRetryPolicy(retryPolicy);
        System.out.println("RetryTemplate configured.");
    }
}
