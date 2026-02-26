package app.integration.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class APIReader
{
    private final ObjectMapper objectMapper;

    public APIReader(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;

        objectMapper.registerModule(new JavaTimeModule());
    }

    public <T> T getAndConvertData(String url, Class<T> tClass)
    {
        try
        {
            JsonNode node = objectMapper.readTree(new URI(url).toURL());
            return objectMapper.treeToValue(node, tClass);
        }
        catch (URISyntaxException e)
        {
            String safeUrl = url.split("\\?")[0]; //in case apiKey is exposed in string
            throw new IllegalArgumentException("Invalid URL: " + safeUrl + " error: " + e.getMessage(), e);
        }
        catch (IOException e)
        {
            String safeUrl = url.split("\\?")[0]; //in case apiKey is exposed in string
            throw new RuntimeException("API call failed for " + safeUrl + ": " + e.getMessage(), e);
        }
    }

    public <T> List<T> getAndConvertDataList(String url, Class<T> tClass)
    {
        try
        {
            JsonNode node = objectMapper.readTree(new URI(url).toURL());

            List<T> list = new ArrayList<>();
            JsonNode resultsNode = node.get("results");

            if (resultsNode != null && resultsNode.isArray())
            {
                for (JsonNode arrayElement : resultsNode)
                {
                    T item = objectMapper.treeToValue(arrayElement, tClass);
                    list.add(item);
                }
            }
            else
            {
                String safeUrl = url.split("\\?")[0]; //in case apiKey is exposed in string
                throw new IllegalArgumentException("api result not an array. URL: " + safeUrl);
            }
            return list;
        }
        catch (URISyntaxException e)
        {
            String safeUrl = url.split("\\?")[0]; //in case apiKey is exposed in string
            throw new IllegalArgumentException("Invalid URL: " + safeUrl + " error: " + e.getMessage(), e);
        }
        catch (IOException e)
        {
            String safeUrl = url.split("\\?")[0]; //in case apiKey is exposed in string
            throw new RuntimeException("API call failed for " + safeUrl + ": " + e.getMessage(), e);
        }
    }
}