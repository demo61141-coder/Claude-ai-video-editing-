package com.example.model

data class ReadyMadeAiPrompt(
    val id: String,
    val badge: String,
    val titleBangla: String,
    val descriptionBangla: String,
    val sampleScriptBangla: String,
    val styleHint: String,
    val defaultChannel: String
)

object AiPromptTemplates {

    val readyMadePrompts: List<ReadyMadeAiPrompt> = listOf(
        ReadyMadeAiPrompt(
            id = "bangla_toon_reel",
            badge = "প্রম্পট ১ • বাংলা টুন নিউজ রিল",
            titleBangla = "২ডি কার্টুন নিউজ রিল (Bangla Toon — News Reel)",
            descriptionBangla = "উপস্থাপক ও মন্ত্রীর অ্যানিমেটেড ২ডি ক্যারেক্টার, খাল খনন/মঞ্চ/পল্লী দৃশ্য ও হাইলাইট শব্দসহ কার্টুন নিউজ ভিডিও তৈরি করবে।",
            sampleScriptBangla = "দেশে কুড়ি হাজার কিলোমিটার খাল খনন হবে। মাননীয় প্রধানমন্ত্রী তারেক রহমান এই কর্মসূচি উদ্বোধন করবেন। আগামী পাঁচ বছরে পঁচিশ কোটি বৃক্ষরোপণ করা হবে। প্রতি বছর পাঁচ কোটি চারা রোপণ করা হবে। আমরা একটি সবুজ বাংলাদেশ গড়তে চাই।",
            styleHint = "backdropPreset হিসেবে canal, stage বা house ব্যবহার করবে; toonWho হিসেবে host বা minister এবং toonPose হিসেবে point বা talk ব্যবহার করবে; প্রতিটি শটে মূল শব্দটি highlightWord (hi) হিসেবে দেবে।",
            defaultChannel = "বাংলা টুন — নিউজ রিল"
        ),
        ReadyMadeAiPrompt(
            id = "breaking_news",
            badge = "প্রম্পট ২ • ব্রেকিং নিউজ",
            titleBangla = "জরুরি ব্রেকিং নিউজ ভিডিও তৈরির প্রম্পট",
            descriptionBangla = "যেকোনো তাজা খবর বা জরুরি ঘোষণার স্ক্রিপ্ট থেকে হাই-ইমপ্যাক্ট লাল ব্রেকিং নিউজ ভিডিও তৈরি করবে।",
            sampleScriptBangla = "রাজধানী ঢাকায় চালু হলো নতুন এলিভেটেড এক্সপ্রেসওয়ে রুট। মাত্র ১৫ মিনিটেই বিমানবন্দর থেকে ফার্মগেট ও মতিঝিল পৌঁছানো সম্ভব হবে। যানজট নিরসনে এটি বড় ভূমিকা রাখবে বলে জানিয়েছেন সংশ্লিষ্ট প্রকৌশলীরা।",
            styleHint = "CRIMSON_BREAKING স্টাইল, DRAMATIC_PUSH ও KEN_BURNS_IN মোশন এবং URGENT_ALERT ও BREAKING_SWOOSH সাউন্ড ইফেক্ট ব্যবহার করবে।",
            defaultChannel = "বাংলা নিউজ ২৪"
        ),
        ReadyMadeAiPrompt(
            id = "prime_bulletin",
            badge = "প্রম্পট ৩ • প্রধান সংবাদ",
            titleBangla = "সন্ধ্যা ৭টার পূর্ণাঙ্গ সংবাদ বুলেটিন প্রম্পট",
            descriptionBangla = "একাধিক খবরের সারসংক্ষেপ থেকে ৪-৬টি দৃশ্যের (Shots) পূর্ণাঙ্গ প্রাইম-টাইম টিভি বুলেটিন তৈরি করবে।",
            sampleScriptBangla = "১. পদ্মা সেতু দিয়ে একদিনে সর্বোচ্চ পণ্যবাহী ট্রাক ও যানবাহন পারাপারের নতুন রেকর্ড সৃষ্টি হয়েছে।\n২. মেট্রোরেলে যাত্রীদের সুবিধার্থে পিক আওয়ারে ট্রেনের সংখ্যা আরও বাড়ানো হয়েছে।\n৩. আগামী ২৪ ঘণ্টায় ঢাকাসহ দেশের বিভিন্ন অঞ্চলে হালকা থেকে মাঝারি বৃষ্টির পূর্বাভাস দিয়েছে আবহাওয়া অধিদপ্তর।",
            styleHint = "NAVY_GOLD_PRIME ও CRIMSON_BREAKING থিম, প্রতিটি খবরের জন্য আলাদা শট, এবং NEWS_CHIME ও BREAKING_SWOOSH সাউন্ড ইফেক্ট ব্যবহার করবে।",
            defaultChannel = "সময় এক্সপ্রেস টিভি"
        ),
        ReadyMadeAiPrompt(
            id = "economy_business",
            badge = "প্রম্পট ৪ • অর্থনীতি ও বাণিজ্য",
            titleBangla = "অর্থনীতি, বাজার দর ও বাণিজ্য প্রতিবেদন প্রম্পট",
            descriptionBangla = "রেমিট্যান্স, রপ্তানি আয়, শেয়ার বাজার বা কৃষি অর্থনীতির খবরকে প্রফেশনাল গ্রাফিক্স ভিডিওতে রূপান্তর করবে।",
            sampleScriptBangla = "চলতি মাসে প্রবাসী আয় বা রেমিট্যান্স প্রবাহে নতুন গতি এসেছে। পাশাপাশি তৈরি পোশাক ও কৃষি প্রক্রিয়াজাত পণ্য রপ্তানিতে ১৫ শতাংশ প্রবৃদ্ধি অর্জিত হয়েছে। ডিজিটাল ব্যাংকিং লেনদেনেও গ্রাহকদের আগ্রহ বাড়ছে।",
            styleHint = "EMERALD_ECONOMY ও NAVY_GOLD_PRIME থিম, PAN_LEFT_RIGHT ও KEN_BURNS_OUT মোশন এবং HEADLINE_IMPACT সাউন্ড ইফেক্ট ব্যবহার করবে।",
            defaultChannel = "বাণিজ্য প্রতিদিন HD"
        )
    )

    /**
     * Builds the complete, copy-ready prompt for ChatGPT / Gemini / Claude.
     * Clearly instructs the AI to read the script at the bottom and output strictly valid JSON
     * for Bangla News Studio Pro (supporting both 2D Toon News Reel scenes and Broadcast Studio scenes).
     */
    fun buildFullCopyablePrompt(
        template: ReadyMadeAiPrompt,
        customUserScript: String
    ): String {
        val finalScript = customUserScript.trim().ifEmpty {
            template.sampleScriptBangla
        }

        return """তুমি একজন প্রফেশনাল টিভি ও কার্টুন নিউজ প্রডিউসার এবং "Bangla News Studio Pro" ভিডিও এডিটর অ্যাপের প্রজেক্ট কোড জেনারেটর।

নিচে দেওয়া সংবাদের স্ক্রিপ্টটি ভালোভাবে পড়ো এবং বুঝে নাও। এরপর ওই স্ক্রিপ্ট অনুযায়ী এই ভিডিও এডিটর অ্যাপের জন্য একটি সম্পূর্ণ, নির্ভুল JSON কোড তৈরি করে দাও, যাতে আমি কোডটি অ্যাপে পেস্ট করলেই সরাসরি অ্যানিমেটেড নিউজ ভিডিও তৈরি হয়ে যায়।

নির্দেশনা:
১. উত্তর হিসেবে শুধুমাত্র বৈধ (Valid) JSON কোড দেবে, কোনো অতিরিক্ত ব্যাখ্যা বা লেখার প্রয়োজন নেই।
২. স্ক্রিপ্টের প্রতিটি বাক্য বা পয়েন্টের জন্য আলাদা দৃশ্য (shots) তৈরি করবে (৪ থেকে ৮টি শট)।
৩. স্টাইল নির্দেশনা: ${template.styleHint}
৪. নিচের JSON স্ট্রাকচারটি হুবহু অনুসরণ করবে:

{
  "projectName": "সংবাদের মূল প্রজেক্টের নাম",
  "channelName": "${template.defaultChannel}",
  "tickerText": "নিচে স্ক্রল করার জন্য সংবাদের মূল পয়েন্টগুলো • দিয়ে যুক্ত করে লিখবে",
  "showTicker": true,
  "shots": [
    {
      "categoryTag": "সংবাদ দৃশ্য ১",
      "headline": "আকর্ষণীয় ও সংক্ষিপ্ত মূল সংলাপ বা সংবাদ শিরোনাম",
      "highlightWord": "শিরোনামের ভেতরের সবচেয়ে গুরুত্বপূর্ণ শব্দ বা সংখ্যা (হলুদ রঙে হাইলাইট হবে)",
      "subheadline": "শিরোনামের নিচে বিস্তারিত উপ-শিরোনাম বা মূল তথ্য",
      "stageBannerText": "৫৫তম জাতীয় সমবায় দিবস",
      "reporterLocation": "ঢাকা স্টুডিও • LIVE",
      "durationSeconds": 6.5,
      "backdropPreset": "canal",
      "toonWho": "host",
      "toonPose": "point",
      "motionPreset": "KEN_BURNS_IN",
      "lowerThirdStyle": "CRIMSON_BREAKING",
      "textAnimation": "SLIDE_UP_REVEAL",
      "motionGraphicOverlay": "FULL_BROADCAST_PACK",
      "soundEffect": "NEWS_CHIME",
      "keyframes": {
        "startScale": 1.0,
        "endScale": 1.12,
        "startOffsetX": 0.0,
        "endOffsetX": 0.0
      }
    }
  ]
}

(নোট:
- backdropPreset এর মান হতে পারে: canal (২ডি খাল খনন ও বৃক্ষরোপণ দৃশ্য), stage (২ডি আলোচনা মঞ্চ দৃশ্য), house (২ডি পল্লী ভবন দৃশ্য), studio_desk (টিভি নিউজরুম), dhaka_metro (মেট্রোরেল), padma_bridge (পদ্মা সেতু)
- toonWho এর মান হতে পারে: host (উপস্থাপক), minister (মন্ত্রী ও উপস্থাপক), minister_solo (শুধু মন্ত্রী), none (কোনো ক্যারেক্টার ছাড়া)
- toonPose এর মান হতে পারে: point (হাত তুলে পয়েন্ট করা), talk (কথা বলার ভঙ্গি)
- motionPreset এর মান হতে পারে: KEN_BURNS_IN, KEN_BURNS_OUT, PAN_LEFT_RIGHT, PAN_RIGHT_LEFT, DRAMATIC_PUSH
- soundEffect এর মান হতে পারে: BREAKING_SWOOSH, NEWS_CHIME, HEADLINE_IMPACT, TICKER_BEEP, URGENT_ALERT)

=========================================
[আমার সংবাদের স্ক্রিপ্ট নিচে দেওয়া হলো — এই স্ক্রিপ্ট দেখেই JSON ফাইল তৈরি করো]:
$finalScript
========================================="""
    }
}
