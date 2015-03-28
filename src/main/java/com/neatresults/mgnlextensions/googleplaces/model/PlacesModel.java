/*
 * Licensed to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neatresults.mgnlextensions.googleplaces.model;

import info.magnolia.context.MgnlContext;
import info.magnolia.dam.api.Asset;
import info.magnolia.dam.templating.functions.DamTemplatingFunctions;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.templatingkit.functions.STKTemplatingFunctions;
import info.magnolia.module.templatingkit.templates.components.AbstractItemListModel;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.templating.functions.TemplatingFunctions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model class for places.
 */
public class PlacesModel extends AbstractItemListModel<TemplateDefinition> {

    private static final Logger log = LoggerFactory.getLogger(PlacesModel.class);

    private Map<String, String> catIcons = new HashMap<String, String>();

    private DamTemplatingFunctions damfn;

    public PlacesModel(Node content, info.magnolia.rendering.template.TemplateDefinition definition, RenderingModel<?> parent, STKTemplatingFunctions stkFunctions, TemplatingFunctions templatingFunctions) {
        super(content, definition, parent, stkFunctions, templatingFunctions);
        damfn = Components.newInstance(DamTemplatingFunctions.class);
    }

    @Override
    public String execute() {
        try {
            // build cat uuid - icon map
            for (Node cat : NodeUtil.asIterable(content.getNodes("categories*"))) {
                String catId = cat.getProperty("category").getString();
                String icon = "";
                if (cat.hasProperty("icon")) {
                    String iconType = cat.getProperty("icon").getString();
                    if ("damIcon".equals(iconType)) {
                        Asset asset = damfn.getAsset(cat.getProperty("damIcon").getString());
                        if (asset != null) {
                            icon = asset.getLink();
                        }
                    } else if (StringUtils.isNotBlank(iconType)) {
                        icon = cat.getProperty(iconType).getString();
                        if (icon.startsWith("/")) {
                            icon = MgnlContext.getContextPath() + icon;
                        }
                    }
                }
                catIcons.put(catId, icon);
            }
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return super.execute();
    }

    @Override
    protected int getMaxResults() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected List<Node> search() throws RepositoryException {
        Session session = MgnlContext.getJCRSession("googlePlaces");
        StringBuilder whereClause = new StringBuilder("where ");
        for (Node cat : NodeUtil.asIterable(content.getNodes("categories*"))) {
            String catId = cat.getProperty("category").getString();
            whereClause.append("contains(categories, '" + catId + "') or ");
        }
        whereClause.setLength(whereClause.length() - 3);
        if (whereClause.length() == 3) {
            whereClause.setLength(0);
        }

        Query query = session.getWorkspace().getQueryManager().createQuery("select * from [mgnl:google-place] " + whereClause.toString(), Query.JCR_SQL2);
        return NodeUtil.asList(NodeUtil.asIterable(query.execute().getNodes()));
    }

    @Override
    protected void filter(List<Node> itemList) {
    }

    @Override
    protected void sort(List<Node> itemList) {
    }

    public void saveLatLong(String path, double latitude, double longitude) {
        try {
            Session session = MgnlContext.getJCRSession("googlePlaces");
            Node node = session.getNode(path);
            node.setProperty("latitude", latitude);
            node.setProperty("longitude", longitude);
            session.save();
        } catch (RepositoryException e) {
            log.warn("unable to save latitude and longitude for path {0}", path, e);
        }
    }

    public Map<String, String> getCategoryToIconMapping() {
        return catIcons;
    }

    public String getIconForPlace(Node place) {
        try {
            if (!place.hasProperty("categories")) {
                return null;
            }
            Value[] vals = place.getProperty("categories").getValues();
            for (Value val : vals) {
                if (catIcons.containsKey(val.getString())) {
                    return catIcons.get(val.getString());
                }
            }
        } catch (RepositoryException e) {
            return null;
        }
        return null;
    }
}
