package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ====================================================
// ALL YELLOW BACKGROUND & NAVY BLUE COLOR PALETTE
// ====================================================

// 1. Navy Blue Elements
val NavyPrimary = Color(0xFF0F172A) // Deep Midnight Navy Blue
val NavySecondary = Color(0xFF1E3A8A) // Royal Navy Blue
val NavySlate = Color(0xFF334155) // Slate Navy
val NavyDarkest = Color(0xFF020617) // Ultra Deep Navy
val OnNavyPrimary = Color(0xFFFACC15) // Bold Yellow on Navy
val NavyContainer = Color(0xFF0F172A) // Dark Navy Container
val OnNavyContainer = Color(0xFFFACC15) // Bold Yellow on Navy

// 2. Yellow Backgrounds & Surfaces (NO White background)
val YellowBackground = Color(0xFFFEF08A) // Vibrant, warm sunny yellow background
val YellowCardSurface = Color(0xFFFEF9C3) // Warm soft cream-yellow card surface
val YellowCardElevated = Color(0xFFFFFBEB) // Luminous sunny cream card surface
val YellowPrimary = Color(0xFFFACC15) // Sunflower Yellow (#FACC15)
val YellowDeep = Color(0xFFEAB308) // Golden Amber Yellow
val YellowLight = Color(0xFFFEF08A) // Soft Luminous Yellow
val YellowContainer = Color(0xFFFEF9C3) // Warm Soft Yellow Container
val OnYellowPrimary = Color(0xFF0F172A) // Deep Navy Blue on Yellow
val OnYellowAccent = Color(0xFF0F172A)

// 3. High-Contrast Typography & Borders (Navy Blue on Yellow)
val DarkNavyText = Color(0xFF0F172A) // Deep midnight navy text (maximum contrast on yellow)
val DarkSlateText = Color(0xFF334155) // Slate navy sub-labels
val NavyBorder = Color(0xFF0F172A) // Midnight navy border
val YellowBorder = Color(0xFFEAB308) // Deep golden yellow border
val SubtleBorder = Color(0xFFCA8A04) // Darker yellow border for clear contrast

// 4. Status Colors
val SuccessGreen = Color(0xFF15803D) // Paid status
val SuccessGreenBg = Color(0xFFDCFCE7)
val SuccessGreenText = Color(0xFF14532D)

val PendingAmber = Color(0xFFC2410C) // Pending status
val PendingAmberBg = Color(0xFFFFEDD5)
val PendingAmberText = Color(0xFF7C2D12)

val AdvanceNavy = Color(0xFF1E3A8A) // Advance Rent in Royal Navy
val AdvanceNavyBg = Color(0xFFDBEAFE)
val AdvanceNavyText = Color(0xFF1E3A8A)

val AlertRed = Color(0xFFDC2626) // Warning/Delete
val AlertRedBg = Color(0xFFFEE2E2)
val AlertRedText = Color(0xFF991B1B)

val LockedBadgeBg = Color(0xFFFEF08A) // Yellow locked badge
val LockedBadgeBorder = Color(0xFFCA8A04)
val LockedBadgeText = Color(0xFF713F12)

// Theme Mappings: Whole Background & Surfaces in Yellow
val LightBackground = YellowBackground
val LightOnBackground = DarkNavyText
val LightSurface = YellowCardSurface
val LightOnSurface = DarkNavyText
val LightSurfaceVariant = YellowContainer
val LightOnSurfaceVariant = DarkNavyText
val LightOutline = NavyBorder
val LightOutlineVariant = YellowBorder

val DarkBackground = YellowBackground
val DarkOnBackground = DarkNavyText
val DarkSurface = YellowCardSurface
val DarkOnSurface = DarkNavyText
val DarkSurfaceVariant = YellowContainer
val DarkOnSurfaceVariant = DarkNavyText
val DarkOutline = NavyBorder
val DarkOutlineVariant = YellowBorder

// Compat Aliases (Map previous PureWhiteCard to YellowCardSurface)
val PureWhiteCard = YellowCardSurface // All white card backgrounds replaced by yellow!
val BackgroundWarmLight = YellowBackground
val BackgroundVeryLight = YellowBackground
val SkyBluePrimary = NavyPrimary
val SkyBlueLight = YellowPrimary
val OnSkyBluePrimary = OnNavyPrimary
val SkyBlueContainer = YellowContainer
val OnSkyBlueContainer = DarkNavyText
val YellowAccent = YellowPrimary
val SkyBlueBorder = YellowBorder
val PrimaryColor = NavyPrimary
val TextPrimary = DarkNavyText
val TextSecondary = DarkSlateText
val CardBorder = YellowBorder
val PaidGreen = SuccessGreen
val PaidGreenBg = SuccessGreenBg
val PaidGreenText = SuccessGreenText
val UnpaidAmber = PendingAmber
val UnpaidAmberBg = PendingAmberBg
val UnpaidAmberText = PendingAmberText
val OverdueRed = AlertRed
val AdvancePurple = AdvanceNavy
val AdvancePurpleBg = AdvanceNavyBg
val AdvancePurpleText = AdvanceNavyText

val PurplePrimary = NavyPrimary
val OnPurplePrimary = OnNavyPrimary
val PurplePrimaryContainer = YellowContainer
val OnPurplePrimaryContainer = DarkNavyText

val PinkSecondary = YellowPrimary
val OnPinkSecondary = OnYellowPrimary
val PinkSecondaryContainer = YellowContainer
val OnPinkSecondaryContainer = DarkNavyText

val FuchsiaTertiary = NavySecondary
val OnFuchsiaTertiary = OnNavyPrimary
val FuchsiaTertiaryContainer = YellowContainer
val OnFuchsiaTertiaryContainer = DarkNavyText

val EmeraldPrimary = NavyPrimary
val OnEmeraldPrimary = OnNavyPrimary
val EmeraldPrimaryContainer = YellowContainer
val OnEmeraldPrimaryContainer = DarkNavyText

val AmberSecondary = YellowPrimary
val OnAmberSecondary = OnYellowPrimary
val AmberSecondaryContainer = YellowContainer
val OnAmberSecondaryContainer = DarkNavyText

val SlateTertiary = NavyPrimary
val OnSlateTertiary = OnNavyPrimary
val SlateTertiaryContainer = YellowContainer
val OnSlateTertiaryContainer = DarkNavyText

val EmeraldDarkPrimary = NavyPrimary
val OnEmeraldDarkPrimary = OnNavyPrimary
val EmeraldDarkPrimaryContainer = YellowContainer
val OnEmeraldDarkPrimaryContainer = DarkNavyText

val AmberDarkSecondary = YellowPrimary
val OnAmberDarkSecondary = OnYellowPrimary
val AmberDarkSecondaryContainer = YellowContainer
val OnAmberDarkSecondaryContainer = DarkNavyText

val SlateDarkTertiary = NavyPrimary
val OnSlateDarkTertiary = OnNavyPrimary
val SlateDarkTertiaryContainer = YellowContainer
val OnSlateDarkTertiaryContainer = DarkNavyText

val PurpleDarkPrimary = NavyPrimary
val OnPurpleDarkPrimary = OnNavyPrimary
val PurpleDarkPrimaryContainer = YellowContainer
val OnPurpleDarkPrimaryContainer = DarkNavyText

val PinkDarkSecondary = YellowPrimary
val OnPinkDarkSecondary = OnYellowPrimary
val PinkDarkSecondaryContainer = YellowContainer
val OnPinkDarkSecondaryContainer = DarkNavyText

val FuchsiaDarkTertiary = NavySecondary
val OnFuchsiaDarkTertiary = OnNavyPrimary
val FuchsiaDarkTertiaryContainer = YellowContainer
val OnFuchsiaDarkTertiaryContainer = DarkNavyText
