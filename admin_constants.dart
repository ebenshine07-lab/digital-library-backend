class AdminConstants {
  static const String baseUrl = 'http://localhost:8080/api/v1';

  static const String login = '$baseUrl/auth/login';
  static const String dashboard = '$baseUrl/admin/dashboard';
  static const String books = '$baseUrl/admin/books';
  static const String categories = '$baseUrl/admin/categories';
  static const String users = '$baseUrl/admin/users';
  static const String bookRequests = '$baseUrl/admin/book-requests';
  static const String accessRequests = '$baseUrl/admin/access-requests';
  static const String activityLogs = '$baseUrl/admin/activity-logs';
  static const String uploadPdf = '$baseUrl/admin/upload/pdf';
  static const String uploadCover = '$baseUrl/admin/upload/cover';
}

