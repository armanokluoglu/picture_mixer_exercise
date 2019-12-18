package com.arman.app;

import android.support.constraint.ConstraintLayout.LayoutParams;
import android.support.v7.widget.CardView;
import java.util.ArrayList;
import java.util.List;

public class CardList {
    private List<CardView> cards;

    public CardList() {
        this.cards = new ArrayList<>();
    }

    public void addCard(CardView card) {
        cards.add(card);
    }

    public List<CardView> getCards() {
        return cards;
    }

    public int size() {
        return cards.size();
    }

    public CardView get(int i) {
        return cards.get(i);
    }
}
