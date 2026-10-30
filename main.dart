import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'core/theme/admin_theme.dart';
import 'presentation/screens/admin_login_screen.dart';
import 'presentation/screens/admin_main_layout.dart';
import 'providers/admin_providers.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(
    const ProviderScope(
      child: DigitalLibraryAdminApp(),
    ),
  );
}

class DigitalLibraryAdminApp extends ConsumerWidget {
  const DigitalLibraryAdminApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final client = ref.watch(adminApiClientProvider);

    return MaterialApp(
      title: 'Digital Library Administrator Console',
      debugShowCheckedModeBanner: false,
      theme: AdminTheme.darkTheme,
      home: FutureBuilder<String?>(
        future: client.getToken(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Scaffold(
              backgroundColor: AdminColors.background,
              body: Center(child: CircularProgressIndicator(color: AdminColors.primary)),
            );
          }
          final token = snapshot.data;
          if (token != null && token.isNotEmpty) {
            return const AdminMainLayout();
          }
          return const AdminLoginScreen();
        },
      ),
    );
  }
}

