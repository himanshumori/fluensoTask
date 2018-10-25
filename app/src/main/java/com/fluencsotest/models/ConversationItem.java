package com.fluencsotest.models;

import com.fluencsotest.Util;

public class ConversationItem {

    private int itemType = Util.PERSONA;// default persona

    private PersonaConvITem personaConvITem;
    private UserConvItem userConvItem;

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public PersonaConvITem getPersonaConvITem() {
        return personaConvITem;
    }

    public void setPersonaConvITem(PersonaConvITem personaConvITem) {
        this.personaConvITem = personaConvITem;
    }

    public UserConvItem getUserConvItem() {
        return userConvItem;
    }

    public void setUserConvItem(UserConvItem userConvItem) {
        this.userConvItem = userConvItem;
    }

    public ConvEntityItem getConvEntityItem() {

        if (itemType == Util.PERSONA) {
            return ((ConvEntityItem) personaConvITem);
        } else {
            return ((ConvEntityItem) userConvItem);
        }
    }
}
