/**
 * Copyright 2009-2018 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.renderkit;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

public class SelectRenderer extends InputRenderer {

    protected boolean isSelected(FacesContext context, UIComponent component, Object itemValue, Object valueArray, Converter converter) {
        if (itemValue == null && valueArray == null) {
            return true;
        }

        if (valueArray != null) {
            if (!valueArray.getClass().isArray()) {
                return valueArray.equals(itemValue);
            }

            int length = Array.getLength(valueArray);
            for (int i = 0; i < length; i++) {
                Object value = Array.get(valueArray, i);

                if (value == null && itemValue == null) {
                    return true;
                }
                else {
                    if ((value == null) ^ (itemValue == null)) {
                        continue;
                    }

                    Object compareValue;
                    if (converter == null) {
                        compareValue = coerceToModelType(context, itemValue, value.getClass());
                    }
                    else {
                        compareValue = itemValue;

                        if (compareValue instanceof String && !(value instanceof String)) {
                            compareValue = converter.getAsObject(context, component, (String) compareValue);
                        }
                    }

                    if (value.equals(compareValue)) {
                        return (true);
                    }
                }
            }
        }
        return false;
    }

    protected int countSelectItems(List<SelectItem> selectItems) {
        if (selectItems == null) {
            return 0;
        }

        int count = selectItems.size();
        for (SelectItem selectItem : selectItems) {
            if (selectItem instanceof SelectItemGroup) {
                count += countSelectItems(((SelectItemGroup) selectItem).getSelectItems());
            }
        }
        return count;
    }

    protected int countSelectItems(SelectItem[] selectItems) {
        if (selectItems == null) {
            return 0;
        }

        int count = selectItems.length;
        for (SelectItem selectItem : selectItems) {
            if (selectItem instanceof SelectItemGroup) {
                count += countSelectItems(((SelectItemGroup) selectItem).getSelectItems());
            }
        }
        return count;
    }

    /**
     * Checks if at least one disabled select item has been submitted - this may occur with client side manipulation (#3264)
     * @throws javax.faces.FacesException if client side manipulation has been detected, in order to reject the submission
     */
    protected void checkDisabledSelectItemSubmitted(FacesContext context, UIInput component, Object[] oldValues, String... newSubmittedValues) 
            throws FacesException {
        String msg = "Disabled select item has been submitted";
        List<Object> oldVals = oldValues == null ? Collections.emptyList() : Arrays.asList(oldValues);
        List<String> newSubmittedValsStr = newSubmittedValues == null ? Collections.<String>emptyList() : Arrays.asList(newSubmittedValues);
        for (SelectItem selectItem : getSelectItems(context, component)) {
            if (selectItem.isDisabled()) {
                String selectItemValStr = getOptionAsString(context, component, component.getConverter(), selectItem.getValue());
                if (oldVals.contains(selectItemValStr) && !newSubmittedValsStr.contains(selectItemValStr)) {
                    // disabled select item has been unselected
                    throw new FacesException(msg);
                }
                if (newSubmittedValsStr.contains(selectItemValStr) && !oldVals.contains(selectItemValStr)) {
                    // disabled select item has been selected
                    throw new FacesException(msg);
                }
            }
        }
    }
    
    
}
