import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class AdminApiException implements Exception {
  final String message;
  final int statusCode;

  AdminApiException(this.message, [this.statusCode = 500]);

  @override
  String toString() => message;
}

class AdminApiClient {
  static const String tokenKey = 'admin_jwt_token';

  Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(tokenKey);
  }

  Future<void> saveToken(String token) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(tokenKey, token);
  }

  Future<void> clearToken() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(tokenKey);
  }

  Future<Map<String, String>> _getHeaders({bool requiresAuth = true}) async {
    final headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    };
    if (requiresAuth) {
      final token = await getToken();
      if (token != null) {
        headers['Authorization'] = 'Bearer $token';
      }
    }
    return headers;
  }

  Future<dynamic> get(String url) async {
    final headers = await _getHeaders();
    final response = await http.get(Uri.parse(url), headers: headers);
    return _handle(response);
  }

  Future<dynamic> post(String url, {dynamic body, bool requiresAuth = true}) async {
    final headers = await _getHeaders(requiresAuth: requiresAuth);
    final response = await http.post(Uri.parse(url), headers: headers, body: body != null ? jsonEncode(body) : null);
    return _handle(response);
  }

  Future<dynamic> put(String url, {dynamic body}) async {
    final headers = await _getHeaders();
    final response = await http.put(Uri.parse(url), headers: headers, body: body != null ? jsonEncode(body) : null);
    return _handle(response);
  }

  Future<dynamic> delete(String url) async {
    final headers = await _getHeaders();
    final response = await http.delete(Uri.parse(url), headers: headers);
    return _handle(response);
  }

  dynamic _handle(http.Response response) {
    dynamic json;
    try {
      json = jsonDecode(response.body);
    } catch (_) {}

    if (response.statusCode >= 200 && response.statusCode < 300) {
      if (json is Map && json.containsKey('data')) return json['data'];
      return json;
    } else {
      String msg = 'Request failed (${response.statusCode})';
      if (json is Map && json.containsKey('message')) msg = json['message'].toString();
      throw AdminApiException(msg, response.statusCode);
    }
  }
}

