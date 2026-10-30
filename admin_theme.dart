import 'package:flutter/material.dart';

class AdminColors {
  static const Color background = Color(0xFF11171E);
  static const Color cardSurface = Color(0xFF18222C);
  static const Color surfaceElevated = Color(0xFF222E3A);

  static const Color primary = Color(0xFFEBB036); // Amber
  static const Color accent = Color(0xFF4FC3F7); // Cyan
  static const Color success = Color(0xFF66BB6A);
  static const Color error = Color(0xFFEF5350);
  static const Color warning = Color(0xFFFFA726);

  static const Color textPrimary = Color(0xFFFFFFFF);
  static const Color textSecondary = Color(0xFF90A4AE);
  static const Color borderSubtle = Color(0x1AFFFFFF);
}

class AdminTheme {
  static ThemeData get darkTheme {
    return ThemeData(
      brightness: Brightness.dark,
      scaffoldBackgroundColor: AdminColors.background,
      primaryColor: AdminColors.primary,
      colorScheme: const ColorScheme.dark(
        primary: AdminColors.primary,
        surface: AdminColors.cardSurface,
        background: AdminColors.background,
      ),
      cardTheme: CardTheme(
        color: AdminColors.cardSurface,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
          side: const BorderSide(color: AdminColors.borderSubtle),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AdminColors.surfaceElevated,
        hintStyle: const TextStyle(color: AdminColors.textSecondary, fontSize: 13),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: AdminColors.borderSubtle),
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AdminColors.primary,
          foregroundColor: const Color(0xFF11171E),
          elevation: 0,
          padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 12),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          textStyle: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
        ),
      ),
    );
  }
}

