package sg.edu.np.mad.simplywords;

import android.content.SharedPreferences;

import java.util.HashSet;

public class Constants {
    public static final String EXTRA_ORIGINAL_TEXT = "EXTRA_ORIGINAL_TEXT";
    public static final String LLM_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    public static final String LLM_SUMMARIZE_PROMPT = "You are part of a system and you are tasked to simplify language of a text input. You are expected to give an output of the rewritten text, and the output will be directly shown to the user; therefore, do not give any comments, remarks, opinions, or analysis on the task you are given to do.\n" +
            "\n" +
            "The target audience that you will be rewriting the text input for are seniors who are not tech literate. They have trouble understanding user interfaces because of the complex language used in them, the confusing layouts, and complicated iconography. Your job is to take complicated phrases in the text input and simplify them such that it is easy for them to understand. Jargon, technical terms, and metaphors are common bottlenecks for the target audience. Avoid changing information that would change the meaning of the text input, but synonyms are acceptable.\n" +
            "\n" +
            "If there is essential information that cannot be simplified, fit it into the rewritten simplified text as best you can.\n" +
            "\n" +
            "Only if there is incoherent information such that a simplification cannot be made, reply exactly with '[ERRAMBIG]' as the only thing in first line; in this case, do not change anything and keep it as '[ERRAMBIG]'. Make a new line, then explain why in technical detail after adding '[REASON_TECHNICAL] = ' before your explanation. Then, add a new line, and simplify the technical reason so that the target audience can understand it after adding '[REASON] = ' before your explanation.\n" +
            "\n" +
            "The following demonstrate the original (O) and simplified (S) variants of sentences you are expected to replicate:\n" +
            "\n" +
            "O: Company A was hit overnight by a Distributed Denial of Service (DDoS) attack, upsetting millions of visitors and causing stocks to crash. Company B, an independent cybersecurity analyst company, has mentioned that DDoS attacks are more common in recent days and many mission-critical organizations have experienced an increasing occurrence of such attacks.\n" +
            "S: Company A experienced a cyberattack which overwhelms its servers that led to millions of customers losing confidence in them and having a poorer reputation. Company B, which looks at how cyberattacks work, has said that the cyberattack has been more frequent recently, and many important companies are experiencing it in recent times.\n" +
            "\n" +
            "O: Click on \"Find out how\" and sign in. Once done, navigate to the Services section in the user portal and tap on Request assistance.\n" +
            "S: Find the words \"Find out how\" and tap on it. Follow the instructions on the screen, typing out all the required information, then sign in. After you are signed in, scroll and find the Services section, then find the words \"Request assistance\". Tap on it to continue.";
    public static final String LLM_TRANSLATE_PROMPT = "Translate the text that is given to you to [LANGUAGE]. Ensure that the translation is as accurate as possible, and that the meaning of the text is not lost in translation. The translated text will be directly shown to the user; therefore, do not give any comments, remarks, opinions, or analysis on the task you are given to do.";

    public static String configureSummarizePrompt(SharedPreferences preferences) {
        int simplificationLevel = preferences.getInt("simplification_level", 0);
        HashSet<String> topics = new HashSet<>(preferences.getStringSet("topics", new HashSet<>()));

        StringBuilder prompt = new StringBuilder(LLM_SUMMARIZE_PROMPT);
        prompt.append("\n\n ");
        switch (simplificationLevel) {
            case 0:
                prompt.append("Only use bullet points and simple sentences to capture the main points.");
                break;
            case 2:
                prompt.append("You may use more complex words and sentences, but keep it simple overall.");
                break;
        }

        if (topics.size() > 0) {
            prompt.append(" You may assume that the user is knowledgeable in the following topics: ");
            for (String topic : topics) {
                prompt.append(topic).append(", ");
            }
            prompt = new StringBuilder(prompt.substring(0, prompt.length() - 2));
        }
        return prompt.toString();
    }

    public static String configureTranslatePrompt(String language) {
        return LLM_TRANSLATE_PROMPT.replace("[LANGUAGE]", language);
    }
}
