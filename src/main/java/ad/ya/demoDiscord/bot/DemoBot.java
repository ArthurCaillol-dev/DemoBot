package ad.ya.demoDiscord.bot;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
// import org.springframework.boot.autoconfigure.SpringBootApplication;

import ad.ya.demoDiscord.bank.Account;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.entities.Member;

// @SpringBootApplication
public class DemoBot extends ListenerAdapter {
    protected final double LIMIT = 0;
    // Exécute du code quand le bot est "activé" sur un serveur
    private List<Account> listAccount = new ArrayList<>();

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        Guild guild = event.getGuild();
        guild.updateCommands()
                .addCommands(
                        Commands.slash("hello", "Say hello"),
                        Commands.slash("solde", "Affiche le solde"),
                        Commands.slash("depot", "Déposer de la monneyy")
                                .addOption(OptionType.NUMBER, "depot", "Montant à déposer", true),
                        Commands.slash("retrait", "Retirer de la monneyyy")
                                .addOption(OptionType.NUMBER, "retrait", "Montant à retirer", true),
                        Commands.slash("tell", "repeat a sentence")
                                .addOption(OptionType.STRING, "text", "text to repeat", true)
                )
                .queue();
        try {
            listAccount = guild.findMembers(member -> !member.getUser().isBot()).get().stream().map(member -> new Account(member)).toList();
        } catch (Exception e) {
            System.out.println("Loading....");
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        // Nom de commande
        String commandName = event.getName();
        // Utilisateur qui a exécuté la commande
        Member author = event.getMember();
        Account owner = listAccount.stream().filter(account -> account.getOwner().equals(author)).findFirst().orElseGet(() -> {
                    Account newAccount = new Account(author);
                    listAccount.add(newAccount);
                    return newAccount;
        });

        try {
            (switch (commandName) {
                case "hello" -> event.reply("Hello World !").setEphemeral(true);
                case "tell" -> event.reply("%s said %s".formatted(
                        author.getEffectiveName(),
                        event.getOption("text").getAsString()
                ));
                case "solde" -> event.reply("Votre solde est de : %.2f€".formatted(owner.getSolde()));
                case "retrait" -> event.reply(owner.getSolde() > event.getOption("retrait").getAsDouble() ?
                        doRetrait(owner, author, event) : "Impossible operation, compte insuffisant : tentative  de retrait de %.2f".formatted(event.getOption("depot").getAsDouble()));
                case "depot" -> event.reply(event.getOption("depot").getAsDouble() > 0 ?
                                doDepot(owner, author, event) : "Impossible operation, chiffre impossible : %.2f".formatted(event.getOption("depot").getAsDouble()));
                default -> event.reply("Commande inconnue");
            }).queue();
        } catch (Exception e) {
            System.out.println("Loading....");
        }
    }

    private String doDepot(Account owner, Member author, @NotNull SlashCommandInteractionEvent event) {
        double solde = owner.getSolde();
        double depot = event.getOption("depot").getAsDouble();

        owner.setSolde(solde + depot);
        return("Vous avez déposé : %.2f€".formatted(depot));
    }

    private String doRetrait(Account owner, Member author, @NotNull SlashCommandInteractionEvent event) {
        double solde = owner.getSolde();
        double retrait = event.getOption("retrait").getAsDouble();

        owner.setSolde(solde - retrait);
        return("Vous avez retiré : %.2f€".formatted(retrait));
    }
}