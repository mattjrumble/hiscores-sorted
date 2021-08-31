package com.topranks;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.util.QuantityFormatter;
import net.runelite.http.api.hiscore.*;
import okhttp3.OkHttpClient;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

import static net.runelite.http.api.hiscore.HiscoreSkill.*;

@Slf4j
public class TopRanksPanel extends PluginPanel {
    private List<JLabel> rankLabels = new ArrayList<>();

    private static final List<HiscoreSkill> SKILLS = ImmutableList.of(
//        ATTACK, HITPOINTS, MINING,
//        STRENGTH, AGILITY, SMITHING,
//        DEFENCE, HERBLORE, FISHING,
//        RANGED, THIEVING, COOKING,
//        PRAYER, CRAFTING, FIREMAKING,
//        MAGIC, FLETCHING, WOODCUTTING,
//        RUNECRAFT, SLAYER, FARMING,
//        CONSTRUCTION, HUNTER,
        ABYSSAL_SIRE, ALCHEMICAL_HYDRA, BARROWS_CHESTS,
        BRYOPHYTA, CALLISTO, CERBERUS,
        CHAMBERS_OF_XERIC, CHAMBERS_OF_XERIC_CHALLENGE_MODE, CHAOS_ELEMENTAL,
        CHAOS_FANATIC, COMMANDER_ZILYANA, CORPOREAL_BEAST,
        DAGANNOTH_PRIME, DAGANNOTH_REX, DAGANNOTH_SUPREME,
        CRAZY_ARCHAEOLOGIST, DERANGED_ARCHAEOLOGIST, GENERAL_GRAARDOR,
        GIANT_MOLE, GROTESQUE_GUARDIANS, HESPORI,
        KALPHITE_QUEEN, KING_BLACK_DRAGON, KRAKEN,
        KREEARRA, KRIL_TSUTSAROTH, MIMIC,
        NIGHTMARE, PHOSANIS_NIGHTMARE, OBOR, SARACHNIS,
        SCORPIA, SKOTIZO, TEMPOROSS,
        THE_GAUNTLET, THE_CORRUPTED_GAUNTLET, THEATRE_OF_BLOOD,
        THEATRE_OF_BLOOD_HARD_MODE, THERMONUCLEAR_SMOKE_DEVIL, TZKAL_ZUK,
        TZTOK_JAD, VENENATIS, VETION,
        VORKATH, WINTERTODT, ZALCANO,
        ZULRAH,
        CLUE_SCROLL_BEGINNER, CLUE_SCROLL_EASY, CLUE_SCROLL_MEDIUM,
        CLUE_SCROLL_HARD, CLUE_SCROLL_ELITE, CLUE_SCROLL_MASTER
    );

    private final IconTextField searchBar;

    private final HiscoreClient hiscoreClient;

    @Override
    public void onActivate()
    {
        super.onActivate();
        searchBar.requestFocusInWindow();
    }

    @Inject
    public TopRanksPanel(OkHttpClient okHttpClient)
    {
        this.hiscoreClient = new HiscoreClient(okHttpClient);

        setBorder(new EmptyBorder(18, 10, 0, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.insets = new Insets(0, 0, 10, 0);

        searchBar = new IconTextField();
        searchBar.setIcon(IconTextField.Icon.SEARCH);
        searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
        searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        searchBar.setMinimumSize(new Dimension(0, 30));
        searchBar.addActionListener(e -> lookup());

        add(searchBar, c);
        c.gridy++;

        JPanel ranksPanel = new JPanel();
        ranksPanel.setLayout(new GridLayout(0, 1));
        ranksPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        for (int i = 0; i < SKILLS.size(); i++) {
            ranksPanel.add(makeRankPanel());
        }

        add(ranksPanel, c);
    }

    private JPanel makeRankPanel() {
        JLabel label = new JLabel();
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(2, 2, 2, 2));
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        rankLabels.add(label);
        panel.add(label);
        return panel;
    }

    public void lookup(String username) {
        searchBar.setText(username);
        lookup();
    }

    private void lookup() {
        final String lookup = searchBar.getText();
        if (Strings.isNullOrEmpty(lookup)) {
            return;
        }
        searchBar.setEditable(false);
        searchBar.setIcon(IconTextField.Icon.LOADING_DARKER);
        hiscoreClient.lookupAsync(lookup, HiscoreEndpoint.NORMAL).whenCompleteAsync((result, ex) ->
            SwingUtilities.invokeLater(() -> {
                if (result == null || ex != null) {
                    searchBar.setIcon(IconTextField.Icon.ERROR);
                    searchBar.setEditable(true);
                    return;
                }
                searchBar.setIcon(IconTextField.Icon.SEARCH);
                searchBar.setEditable(true);
                applyHiscoreResult(result);
            })
        );
    }

    private void applyHiscoreResult(HiscoreResult result) {
        // Sort all the skill results by rank (most impressive results go at the start of the list).
        List<PlayerSkillRank> ranks = new ArrayList<>();
        for (HiscoreSkill skill : SKILLS) {
            ranks.add(new PlayerSkillRank(skill, result.getSkill(skill).getRank(), result.getSkill(skill).getLevel()));
        }
        Collections.sort(ranks, (s1, s2) -> {
            if (s1.rank.equals(s2.rank)) {
                return 0;
            } else if (s2.rank == -1) {
                return -1;
            } else if (s1.rank == -1)  {
                return 1;
            } else if (s1.rank < s2.rank) {
                return -1;
            } else {
                return 1;
            }
        });
        log.info(String.valueOf(ranks));
        for (int i = 0; i < ranks.size(); i++) {
            PlayerSkillRank rank = ranks.get(i);
            JLabel label = rankLabels.get(i);
            String labelText;
            if (rank.rank.equals(-1)) {
                labelText = "Unranked - " + rank.skill.getName();
            } else {
                labelText = "#" + QuantityFormatter.formatNumber(rank.rank) + " - " + rank.skill.getName() + " - " + rank.level;
            };
            label.setText(labelText);
        }
    }
}

class PlayerSkillRank {
    public HiscoreSkill skill;
    public Integer rank;
    public Integer level;
    public PlayerSkillRank(HiscoreSkill skill, Integer rank, Integer level) {
        this.skill = skill;
        this.rank = rank;
        this.level = level;
    }
}
