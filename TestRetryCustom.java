import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import java.util.Collections;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;

public class TestRetryCustom {
    public static void main(String[] args) {
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(25000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(60000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, Collections.singletonMap(Exception.class, true)) {
            @Override
            public boolean canRetry(org.springframework.retry.RetryContext context) {
                Throwable t = context.getLastThrowable();
                if (t != null && !super.canRetry(context)) {
                    return false;
                }
                if (t != null) {
                    String msg = t.getMessage() != null ? t.getMessage() : "";
                    if (t instanceof NonTransientAiException || msg.contains("429")) {
                        return true;
                    }
                    if (t instanceof TransientAiException) {
                        return true;
                    }
                    return false; // Stop retrying for other exceptions
                }
                return true;
            }
        };
        retryTemplate.setRetryPolicy(retryPolicy);
        System.out.println("Compiles!");
    }
}
