# Adorn — Women's Luxury Wardrobe, Jewelry Box & AI Stylist 👗💎👜

An Android application built with **Jetpack Compose**, **Material 3**, and modern Kotlin MVVM architecture. Designed as an all-in-one digital boutique closet where women can upload, organize, edit, and style their entire wardrobe: **clothes & ethnic wear (sarees, lehengas, dresses, kurtis, activewear), precious jewelry (earrings, chains, necklaces, rings), luxury handbags & clutches, and fine timepieces**.

---

## 📱 Live App Screenshots

| 👗 Main Wardrobe & Gallery | 🛍️ Dedicated Wishlist & Target Valuation |
| :---: | :---: |
| <img src="docs/screenshots/android_wardrobe_now.png" width="360" alt="Wardrobe Gallery Screen" /> | <img src="docs/screenshots/android_wishlist_tab_live.png" width="360" alt="Wishlist Screen" /> |
| *Real-time collection valuation, category filters, quick search, and multi-select* | *Aspirational pieces, priority tiers (🔥 High, ⭐ Target), and total wishlist budget* |

| 🔘 Multi-Select Mode | 🗑️ Batch Deletion & Actions |
| :---: | :---: |
| <img src="docs/screenshots/android_select_mode_on.png" width="360" alt="Select Mode Active" /> | <img src="docs/screenshots/android_items_selected_live.png" width="360" alt="Items Selected Live" /> |
| *Clean checkmark badges on every card with contextual counter* | *Floating glass action bar for bulk deletion or wardrobe promotion* |

---

## Key Features

### 1. Clothes & Outfits Management 👗🥻
- **Clothing Categories**:
  - `Sarees & Ethnic`: Silk sarees, Kanjeevarams, lehengas, anarkalis.
  - `Dresses & Gowns`: Midi dresses, evening gowns, maxi dresses.
  - `Kurtis & Tops`: Daily kurtas, tunic tops, shirts.
  - `Bottoms & Skirts`: Palazzos, lehenga skirts, trousers.
  - `Sports & Activewear`: Gym sets, yoga leggings, athletic tops, seamless activewear.
- **Fabrics & Materials**:
  - `Pure Silk / Kanjeevaram`
  - `Cotton & Linen`
  - `Georgette / Organza / Chiffon`
  - `Velvet & Satin`
  - `Spandex / Dry-Fit / Technical Athletic`
- **Clothing Sizing**:
  - One-tap quick size pills: `XS (34)`, `S (36)`, `M (38)`, `L (40)`, `XL (42)`, `Free Size`, `6.3m Saree (with blouse)`.
  - Custom sizing support for tailored outfits.

### 2. Dedicated Wishlist & Aspirational Goals 🛍️✨
- **Aspirational Collection Tracking**:
  - Maintain a separate dream wishlist of future luxury acquisitions (e.g., Sabyasachi bridal wear, Cartier watches, Tiffany jewelry).
  - Track **Target Valuation** (`Total Wishlist Budget`) with live piece counters.
- **Priority Tiering**:
  - Filter chips: `All Wishlist`, `🔥 High Priority`, `⭐ Target`, `Aspirational`.
- **One-Tap "Acquired!" Promotion**:
  - Directly promote any wishlist piece to your active wardrobe in one tap (`✨ Acquired! Move to Wardrobe`), automatically updating your collection valuation.

### 3. Multi-Select & Batch Operations ⚡
- **Multi-Select Toggle**: Tap `✓ Select` in the header or long-press cards to enter selection mode.
- **Batch Actions**:
  - Select multiple pieces with circular rose-gold checkmarks.
  - Floating dark glass action bar: **Bulk Delete** with safety confirmation dialog, or **Batch Promote to Wardrobe** from wishlist.
- **Smart Duplicate Cleaner**:
  - Overflow menu tool (`Clean Duplicates`) instantly detects and safely purges redundant duplicate entries while keeping your original items intact.

### 4. AI Stylist Studio — Outfit & Jewelry Coordination ✨
- **Select an Outfit from Your Wardrobe**:
  - Visual horizontal carousel displaying your uploaded sarees, dresses, and kurtis.
  - Tap any dress to make it the **anchor piece** of your look.
  - Auto-adapts occasion and color suggestions to match your selected outfit.
- **AI Curated Ensembles**:
  - Heuristic styling engine coordinates your chosen outfit with matching jewelry sets, earrings, chains, handbags/clutches, and watches from your own collection!
  - Color harmony, metal palette matching, and occasion intelligence.
  - High harmony score & personalized stylist advice narrative.
- **Future API Architecture**:
  - Clean `JewelrySuggestionApiService` interface ready to connect to Gemini Vision, OpenAI, or custom FastAPI backend.

### 5. Matching Sets & Combos 💫
- **Category & Set Toggle**: Tag items as matching sets with a single switch.
- **Quick Combo Chips**: `Necklace + Earrings`, `Chain + Studs`, `Choker + Jhumkas`, `Chain + Bracelet`, `Full Bridal Set`.
- **Visual Set Highlights**: Distinctive `✨ Set` badge on gallery cards and luxury rose gold banner in the detail view.

### 6. Dimensions & Sizing Across All Categories 📏
- **Chains & Necklaces**: `16" Choker`, `18" Princess`, `20" Matinee`, `24" Opera`.
- **Bangles & Bracelets**: Indian bangle sizing (`2.4`, `2.6`, `2.8`) or wrist circumference (`7.0" Wrist`).
- **Rings**: Indian & US ring sizing (`Size 12 Indian / 6 US`).
- **Handbags**: Dimensions (`28cm x 18cm x 8cm`).
- **Clothes**: Size chart chips and custom measurements.

### 7. Multi-Image Support & Direct Myntra / Web Downloader 📸🌐
- **Up to 10 Photos per Product**: Swipeable `HorizontalPager` with indicators in detail view and cover badge in gallery.
- **Direct Web Image Downloader**: Paste any product image URL from Myntra, Nykaa, Ajio, Zara, or Amazon; the app automatically downloads and persists it to local app storage.

### 8. Brand & Designer Selector 🏷️
- Dropdown with 30+ popular brands across Indian jewelry, global luxury, fashion, and ethnic wear:
  - *Jewelry*: Tanishq, CaratLane, Mia, Malabar Gold, Kalyan, Giva, Bluestone, Sabyasachi, Tiffany & Co., Cartier.
  - *Clothes & Ethnic*: Nalli Silks, Biba, FabIndia, W for Woman, Zara, H&M, Sabyasachi, Lululemon.
  - *Handbags & Watches*: Michael Kors, Coach, Titan Raga, Kate Spade, Baggit, Fossil.
  - *Family & Custom*: Family Heirloom, Local Jeweler / Tailor.

### 9. Before & After Trip Packing Checklist 🧳🏡
- **Packing Mode**: Select which pieces you're taking on a trip and check them off into your travel pouch.
- **Safe Return Mode**: Check off items when unpacking at home with built-in **storage location reminders** (e.g. *"Return to: Velvet Vanity Box - Top Slot 1"*).
- **Safety Warning Alert**: Flags missing or unpacked items so valuable pieces are never left behind in hotel safes.

### 10. Women-Friendly Luxury Aesthetic & Indian Rupee Formatting 💰
- Soft rose gold, blush pink, champagne gold, and clean card surfaces.
- 4-tab luxury navigation bar: **Wardrobe**, **Wishlist**, **Stylist**, and **Travel**.
- All valuations and prices formatted in Indian Rupees (`₹`), with total collection valuation tracking.

---

## Tech Stack & Architecture

- **Language**: Kotlin 2.0
- **UI Toolkit**: Jetpack Compose + Material 3
- **Architecture**: MVVM with Kotlin StateFlow & Coroutines
- **Image Loading**: Coil 2.6 with local file caching & disk persistence
- **Local Database**: SQLite with schema versioning (v4) & automated migration handlers
- **Target SDK**: Android 14 (API 34)

---

## How to Install and Run

1. **Direct ADB Install**:
   ```bash
   ~/Library/Android/sdk/platform-tools/adb install -r "app/build/outputs/apk/debug/app-debug.apk"
   ```

2. **Open in Android Studio**:
   - Open `/Users/manikantaamara/Desktop/Antigravity/Women's Wardrobe` in Android Studio.
   - Run on emulator or connected device.
