import org.springframework.retry.support.RetryTemplate;
public class TestRetry {
    public static void main(String[] args) {
        RetryTemplate template = new RetryTemplate();
        System.out.println("RetryTemplate found!");
    }
}
