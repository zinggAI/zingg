package zingg.common.client.arguments.util;

import zingg.common.client.ZinggClientException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvironmentVariableSubstitutor {
    private static final String PATTERN = "\\$(.+?)\\$";

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
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}

