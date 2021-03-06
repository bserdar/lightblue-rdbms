/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.metadata.rdbms.model;

import com.redhat.lightblue.common.rdbms.RDBMSConstants;
import com.redhat.lightblue.metadata.parser.MetadataParser;
import java.util.ArrayList;

public class IfNot extends If<If, If> {
    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        if (getConditions() == null || getConditions().size() != 1) {
            throw com.redhat.lightblue.util.Error.get(RDBMSConstants.ERR_FIELD_REQUIRED, "not doesn't have just one conditional");
        }
        If o = getConditions().get(0);
        T eT = p.newNode();
        o.convert(p, lastArrayNode, eT);
        if (lastArrayNode == null) {
            p.putObject(node, "not", eT);
        } else {
            T iT = p.newNode();
            p.putObject(iT, "not", eT);
            p.addObjectToArray(lastArrayNode, iT);
        }
    }

    @Override
    public <T> If parse(MetadataParser<T> p, T ifT) {
        If x = null;
        T notIfT = p.getObjectProperty(ifT, "not");
        if (notIfT != null) {
            If y = (If) If.getChain().parse(p, notIfT);
            x = new IfNot();
            x.setConditions(new ArrayList());
            x.getConditions().add(y);
        }
        return x;
    }
}
