package com.loohp.interactivechat.objectholders;

import java.util.Objects;

public class ReplaceTextBundle implements Comparable<ReplaceTextBundle> {

    private final String placeholder;
    private final ICPlayer player;
    private final String replaceText;

    public ReplaceTextBundle(String placeholder, ICPlayer player, String replaceText) {
        this.placeholder = placeholder;
        this.player = player;
        this.replaceText = replaceText;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public ICPlayer getPlayer() {
        return player;
    }

    public String getReplaceText() {
        return replaceText;
    }

    @Override
    public int compareTo(ReplaceTextBundle anotherReplaceTextBundle) {
        int compare = Integer.compare(placeholder.length(), anotherReplaceTextBundle.placeholder.length());
        if (compare != 0) {
            return compare;
        } else {
            return Integer.compare(replaceText.length(), anotherReplaceTextBundle.replaceText.length());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReplaceTextBundle that = (ReplaceTextBundle) o;
        return placeholder.equals(that.placeholder) && player.equals(that.player) && replaceText.equals(that.replaceText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeholder, player, replaceText);
    }

}
