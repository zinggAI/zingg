package zingg.common.client.arguments.loader.template;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.ZinggClientException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvironmentVariableSubstitutor {
    private static final String PATTERN = "\\$(.+?)\\$";
    private static final Log LOG = LogFactory.getLog(EnvironmentVariableSubstitutor.class);
    private static final String ENV_VAR_MARKER_START = "$";
    private static final String ENV_VAR_MARKER_END = "$";

    public String substitute(String template, Map<String, String> variables) throws ZinggClientException {
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(template);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            String var = matcher.group(1);
            String value = variables.get(var);

            if (value == null || value.isEmpty()) {
                throw new ZinggClientException("Environment variable $" + var + "$ is not set or empty");
            }

            matcher.appendReplacement(buffer, Matcher.quoteReplacement(value));
            LOG.warn("The variable " + ENV_VAR_MARKER_START + matcher.group(1) + ENV_VAR_MARKER_END
                    + " has been substituted");
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}

