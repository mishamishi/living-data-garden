package com.example.livingdatagarden.data

/** One short, warm sentence (or two) for the speech bubble. Words used: air / water / light. */
fun gardenMessage(snapshot: GardenSnapshot): String {
    val flowers = snapshot.flowers
    if (flowers.all { it.state == FlowerState.BLOOMING }) {
        return "Air’s quality is great today, so the air flower bloomed. " +
            "Water and light are doing great as well."
    }

    // Worst flower leads (ties go to air, then water, then light).
    val lead = flowers.minByOrNull { it.health }!!
    val others = flowers.filter { it !== lead }
    return leadSentence(lead) + " " + othersSentence(others)
}

private fun leadSentence(f: FlowerReading): String = when (f.kind) {
    FlowerKind.AIR -> when (f.state) {
        FlowerState.BLOOMING -> "Air’s quality is great today, so the air flower bloomed."
        FlowerState.STRESSED -> "Air’s quality is so-so today, so the air flower is a little stressed."
        FlowerState.WILTED -> "Air’s quality is poor today, so the air flower wilted."
    }
    FlowerKind.WATER -> when (f.state) {
        FlowerState.BLOOMING -> "The soil is nicely moist, so the water flower bloomed."
        FlowerState.STRESSED -> "The soil is getting dry, so the water flower is a little stressed."
        FlowerState.WILTED -> "The soil is very dry, so the water flower wilted."
    }
    FlowerKind.ENERGY -> when (f.state) {
        FlowerState.BLOOMING -> "Sunlight is strong today, so the light flower bloomed."
        FlowerState.STRESSED -> "Light is soft today, so the light flower is a little stressed."
        FlowerState.WILTED -> "Light is very low, so the light flower wilted."
    }
}

private fun name(f: FlowerReading): String = when (f.kind) {
    FlowerKind.AIR -> "air"
    FlowerKind.WATER -> "water"
    FlowerKind.ENERGY -> "light"
}

private fun mood(s: FlowerState): String = when (s) {
    FlowerState.BLOOMING -> "doing great"
    FlowerState.STRESSED -> "a bit stressed"
    FlowerState.WILTED -> "struggling"
}

private fun othersSentence(others: List<FlowerReading>): String {
    val (a, b) = others
    val first = name(a).replaceFirstChar { it.uppercase() }
    val second = name(b)
    return if (a.state == b.state) {
        val tail = if (a.state == FlowerState.BLOOMING) "as well" else "too"
        "$first and $second are ${mood(a.state)} $tail."
    } else {
        "$first is ${mood(a.state)}; $second is ${mood(b.state)}."
    }
}
