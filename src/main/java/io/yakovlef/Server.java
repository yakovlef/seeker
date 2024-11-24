package io.yakovlef;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        Game game = new Game();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/world", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, gson.toJson(gson.toJsonTree(game)));
            } else {
                sendJsonResponse(exchange, "Method Not Allowed", 405);
            }
        });

        server.createContext("/player", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                if (query == null) {
                    sendJsonResponse(exchange, "{\"error\": \"Missing query parameters\"}", 400);
                    return;
                }

                Map<String, String> params = parseQueryParams(query);

                int x = Integer.parseInt(params.get("x"));
                int y = Integer.parseInt(params.get("y"));

                if (isNeighbor(game.getPlayer().getX(), game.getPlayer().getY(), x, y)) {
                    game.getPlayer().move(x, y);

                    sendJsonResponse(exchange, gson.toJson(gson.toJsonTree(game.getPlayer())));
                }

                sendJsonResponse(exchange, createErrorResponse("x or y is not correct"));
            } else {
                sendJsonResponse(exchange, "Method Not Allowed", 405);
            }
        });

        server.start();

        System.out.println("Server started on port 8080");
    }

    public static boolean isNeighbor(int x, int y, int x2, int y2) {
        int dx = Math.abs(x2 - x);
        int dy = Math.abs(y2 - y);

        return (dx <= 1 && dy <= 1) && !(dx == 0 && dy == 0);
    }

    private static String createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "error");
        response.addProperty("message", message);
        return gson.toJson(response);
    }

    private static String createSuccessResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "success");
        response.addProperty("message", message);
        return gson.toJson(response);
    }

    private static void sendJsonResponse(HttpExchange exchange, String json) throws IOException {
        sendJsonResponse(exchange, json, 200);
    }

    private static void sendJsonResponse(HttpExchange exchange, String json, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, json.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(json.getBytes());
        }
    }

    private static Map<String, String> parseQueryParams(String query) throws IOException {
        Map<String, String> params = new HashMap<>();
        String[] pairs = query.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");

            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);

            if (keyValue.length > 1) {
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                params.put(key, value);
            } else {
                params.put(key, "");
            }
        }

        return params;
    }
}