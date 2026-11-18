# Where the garden gets its data ✿ ⋆˚࿔

The garden listens to the real world.
Each flower reads one number from [Open-Meteo](https://open-meteo.com). It is free. No API key, no account.

## What each flower listens to ⚘.⋆˚࿔

| flower | data | bloom | stressed | wilted |
|---|---|---|---|---|
| Air | US AQI | 50 or less | 51 to 100 | over 100 |
| Water | soil moisture (0–1 cm) | 20% or more | 10% to 20% | under 10% |
| Energy / light | sunlight (W/m²) | 500 W/m² or more | 200 to 500 W/m² | under 200 W/m² |

- Water is shown as %.
- Light is shown as % of 1000 W/m².
- At night, the light flower uses the day's brightest hour. So it doesn't wilt every night.
- Stressed and wilted flowers just fade the bloom drawing for now. It's a placeholder until the real drawings exist.

## Change the place 𖡼.𖤣 𖥧

The garden is fixed to Mexico City.
To change it, open `data/GardenModels.kt` and edit `GardenLocation.MexicoCity` (city, lat, lon).

## Tweak the numbers ✽

All the thresholds live in `data/FlowerRules.kt`. One place, no hunting.
The speech bubble sentence comes from `data/GardenMessage.kt`.

## Live or Manual ✿

- **Live**: refreshes every 15 min.
- **Manual**: only refreshes when you tap the "Updated…" label.

## Keeping it safe ⸙

- No API keys or secrets in the app.
- HTTPS only. Plain http is blocked.
- Only the INTERNET permission. No location permission, so the phone's location is never used.
- Responses are size-limited and checked before we show them.
- Errors show a friendly line. Never technical details.
- Open-Meteo data is CC BY 4.0. The attribution is shown on screen.

## Not connected (yet) ˚࿔

- **Electricity Maps**: needs a private API key. Anyone can pull a key out of an app, so if we add it, it will go through a tiny server (a proxy).
- **CONAGUA**: not connected.
- The on-screen credits say Open-Meteo for all three flowers.

## Code map ⋆˚

Paths start at `app/src/main/java/com/example/livingdatagarden/`.

- `data/GardenModels.kt`: the shapes of the data
- `data/OpenMeteoApi.kt`: talks to Open-Meteo
- `data/OpenMeteoGardenRepository.kt`: turns the answers into a garden
- `data/FlowerRules.kt`: the thresholds
- `data/GardenMessage.kt`: the speech bubble sentence
- `GardenViewModel.kt`: keeps the garden fresh, shared by all screens

## Run the tests ✿

```
./gradlew testDebugUnitTest
```

18 tests. No internet needed.
