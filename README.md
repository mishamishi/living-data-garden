# Living Data Garden ⚘.⋆˚࿔ ✽
An IoT- and AI-enabled environmental data experience that transforms real-time conditions into a living, interactive garden people can understand and act on.
   ✿ This repo is the Android app that will anable users to visualize the digital garden.

# Categories
Data Visualization, Illustration, Motion Graphics, Front-End Development, Physical Computing, AI

# Read the case study
https://readymag.website/u1098496475/5404817/welcometothegarden/

𖡼.𖤣 𖥧 𖡼.𖤣𖥧 . 𖡼.𖤣 𖥧 𖡼.𖤣 𖥧

## Status
Early stage. The app now has the visual interface, navigation and live data (Open-Meteo). Forecasting and AI features are still being drafted, not built yet.

## Screens
1. **Loading**: logo and tagline, then moves to Home automatically
2. **Home**: the garden with the three flowers
3. **Dashboard**: environmental readings for the monitored location

Flow: Loading → Home ⇄ Dashboard

<img width="1293" height="854" alt="image" src="https://github.com/user-attachments/assets/13b4d020-b749-4081-bc09-b53eda6eab0c" />

## Concept ✽ ✽ ✽
- One flower per data type (air, water, energy/light)
- Three illustrated states per flower: wilted, stressed, bloom
- Monitors one fixed location (a school, workplace or community), not arbitrary user locations

## Tech 
- Kotlin + Jetpack Compose
- Min SDK: API 24
- Design and prototype in Figma
- Font: CS Sullivan Pixel
- Live data: Open-Meteo (free, no API key)
- Kotlin coroutines + ViewModel

## How the flowers grow ✿
- Air flower → air quality (US AQI)
- Water flower → soil moisture
- Energy/light flower → sunlight

All three read from Open-Meteo. More in [docs/DATA.md](docs/DATA.md)

## Just Getting started ⸙ ˚࿔
