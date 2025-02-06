package ad.ya.demoDiscord;

import ad.ya.demoDiscord.bot.DemoBot;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class DemoDiscordApplication {

    public static void main(String[] args) {
        String token = args[0];
        // Crée une instance de démarrage pour le bot identifié par le token
        System.out.println("Application démarrée !");
        JDABuilder.createDefault(token)
                // configuration pour la bonne exécution :
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_VOICE_STATES
                )
                .enableCache(CacheFlag.VOICE_STATE)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setBulkDeleteSplittingEnabled(false)
                // Associe une classe contenant du code indiquant au bot comment agir
                .addEventListeners(new DemoBot())
                // Message d'activité affiché sur le bot
                .setActivity(Activity.listening("instructions"))
                .build();
            System.out.println("Application finie !");
    }
}
