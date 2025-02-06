package ad.ya.demoDiscord.bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ad.ya.demoDiscord.bank.Account;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

@SpringBootApplication
public class DemoBot extends ListenerAdapter {
    private List<Account> listAccount = new ArrayList<>();
    private final Map<String, Consumer<SlashCommandInteractionEvent>> commandMap = new HashMap<>();

    public DemoBot() {
        commandMap.put("hello", this::handleHello);
        commandMap.put("tell", this::handleTell);
        commandMap.put("solde", this::handleSolde);
        commandMap.put("depot", this::handleDepot);
        commandMap.put("retrait", this::handleRetrait);
        commandMap.put("travail", this::handleTravail);
        commandMap.put("send", this::handleChange);
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        Guild guild = event.getGuild();
        guild.updateCommands()
                .addCommands(
                        Commands.slash("hello", "Say hello"),
                        Commands.slash("solde", "Affiche le solde"),
                        Commands.slash("depot", "Déposer de l'argent")
                                .addOption(OptionType.NUMBER, "depot", "Montant à déposer", true),
                        Commands.slash("retrait", "Retirer de l'argent")
                                .addOption(OptionType.NUMBER, "retrait", "Montant à retirer", true),
                        Commands.slash("travail", "Travail dur"),
                        Commands.slash("send", "Envoie d'argent à un proche")
                                .addOption(OptionType.NUMBER, "argent", "Montant à Envoyer", true)
                                .addOption(OptionType.USER, "receiver", "Personne à Envoyer", true)
                )
                .queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();

        if (commandMap.containsKey(commandName)) {
            commandMap.get(commandName).accept(event);
        } else {
            event.reply("❓ Commande inconnue !").queue();
        }
    }

    private void handleHello(SlashCommandInteractionEvent event) {
        event.reply("👋 Hello World !").setEphemeral(true).queue();
    }

    private void handleTell(SlashCommandInteractionEvent event) {
        String text = event.getOption("text").getAsString();
        String userName = event.getMember().getEffectiveName();
        event.reply("🗣️ %s said: \"%s\"".formatted(userName, text)).queue();
    }

    private void handleSolde(SlashCommandInteractionEvent event) {
        Account owner = getOrCreateAccount(event.getMember());
        event.reply("💰 Votre solde est de : **%.2f€**".formatted(owner.getSolde())).queue();
    }

    private void handleDepot(SlashCommandInteractionEvent event) {
        Account owner = getOrCreateAccount(event.getMember());
        double amount = event.getOption("depot").getAsDouble();
        event.reply(amount > 0 ? doDepot(owner, event) :
                "⚠️ Opération impossible, montant invalide : **%.2f€**".formatted(amount)).queue();
    }

    private void handleRetrait(SlashCommandInteractionEvent event) {
        Account owner = getOrCreateAccount(event.getMember());
        double amount = event.getOption("retrait").getAsDouble();
        event.reply(owner.getSolde() > amount && amount > 0 ? doRetrait(owner, event) :
                "❌ Opération impossible ! Tentative de retrait de **%.2f€**.".formatted(amount)).queue();
    }

    private void handleTravail(SlashCommandInteractionEvent event) {
        event.reply("💼 Bonne chance !").queue();
    }

    private void handleChange(SlashCommandInteractionEvent event) {
        String receiver = event.getOption("receiver").getAsString();
        Account owner = getOrCreateAccount(event.getMember());
        Account toMember = getOrCreateAccount(event.getOption("receiver").getAsMember());
        double soldeOwner = owner.getSolde();
        double soldeReceiver = toMember.getSolde();
        double monney = event.getOption("argent").getAsDouble();

        if (monney > 0 && soldeOwner > monney) {
            owner.setSolde(soldeOwner - monney);
            toMember.setSolde(soldeReceiver + monney);
            System.out.println("soldOwner = " + soldeOwner + " && receiver solde = " + soldeReceiver);
            event.reply("💸 Vous avez envoyé : **%.2f€** à %s".formatted(monney, receiver)).queue();
        } else {
            event.reply("❌ Opération impossible : **%.2f€** à %s".formatted(monney, receiver)).queue();
        }
    }

    private Account getOrCreateAccount(Member member) {
        return listAccount.stream()
                .filter(account -> account.getOwner().equals(member))
                .findFirst()
                .orElseGet(() -> {
                    Account newAccount = new Account(member);
                    listAccount.add(newAccount);
                    return newAccount;
                });
    }

    private String doDepot(Account owner, @NotNull SlashCommandInteractionEvent event) {
        double solde = owner.getSolde();
        double depot = event.getOption("depot").getAsDouble();

        owner.setSolde(solde + depot);
        return "✅ Vous avez déposé : **%.2f€**".formatted(depot);
    }

    private String doRetrait(Account owner, @NotNull SlashCommandInteractionEvent event) {
        double solde = owner.getSolde();
        double retrait = event.getOption("retrait").getAsDouble();

        owner.setSolde(solde - retrait);
        return "💸 Vous avez retiré : **%.2f€**".formatted(retrait);
    }

}