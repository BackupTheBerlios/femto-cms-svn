/*
 * femtocms minimalistic content management.
 * Copyright(C) 2005-2006 mobizcorp Europe Ltd., all rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.mobizcorp.femtocms.engine;

import static de.mobizcorp.femtocms.prefs.ServerPreferences.RESOURCE_CACHE_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.RESOURCE_CACHE_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.TEMPLATE_CACHE_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.TEMPLATE_CACHE_PREFERENCE;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import de.mobizcorp.femtocms.prefs.ServerPreferences;
import de.mobizcorp.lib.Text;
import de.mobizcorp.lib.TwoQueueCache;
import de.mobizcorp.水星.Changes;
import de.mobizcorp.水星.Store;
import de.mobizcorp.水星.Version;
import de.mobizcorp.水星.Changes.LogEntry;
import de.mobizcorp.水星.Manifest.Entry;

/**
 * @author Copyright(C) 2005-2006 mobizcorp Europe Ltd., all rights reserved.
 */
public class ViewEngine extends NullEngine {

    /**
     * Date format in the tip info. Sample:
     * <code>Sun Jul 24 07:56:01 2005</code>
     */
    private static final SimpleDateFormat tipDateFormat = new SimpleDateFormat(
            "EEE MMM dd HH:mm:ss yyyy");

    private long tipLastModified;

    private final TwoQueueCache<String, Templates> templateCache;

    private final TwoQueueCache<Version, byte[]> resourceCache;

    private final Store store;

    private String tip;

    private boolean dirty;

    private Version currentVersion;

    private HashMap<Text, Entry> currentManifest;

    public ViewEngine(File base) throws IOException {
        super(base);
        this.store = new Store(Store.findBase(base));
        this.templateCache = new TwoQueueCache<String, Templates>(
                ServerPreferences.getInt(TEMPLATE_CACHE_PREFERENCE,
                        TEMPLATE_CACHE_FALLBACK));
        this.resourceCache = new TwoQueueCache<Version, byte[]>(
                ServerPreferences.getInt(RESOURCE_CACHE_PREFERENCE,
                        RESOURCE_CACHE_FALLBACK));
        this.dirty = true;
    }

    @Override
    public long getLastModified(String href) {
        try {
            Text path = Text.urlDecode(relativize(this.baseUri, new URI(href)));
            if (isInManifest(path)) {
                return tipLastModified;
            } else {
                return 0;
            }
        } catch (Exception e) {
            return super.getLastModified(href);
        }
    }

    @Override
    protected StreamResource createStreamSource(String href) throws IOException {
        final Text path = Text.urlDecode(href);
        final Entry entry = getManifest().get(path);
        if (entry == null) {
            return super.createStreamSource(href);
        }
        byte[] data = resourceCache.get(entry.version);
        if (data == null) {
            data = store.file(path).read(entry.version);
            resourceCache.put(entry.version, data);
        }
        StreamResource result = new StreamResource();
        result.setLastModified(tipLastModified);
        result.setSystemId(baseUri.resolve(URI.create(href)).toString());
        result.setInputStream(new ByteArrayInputStream(data));
        return result;
    }

    private void initManifest() throws IOException {
        final Changes changes = store.changes();
        final Version currentTip = changes.tip();
        if (currentVersion != null && currentVersion.equals(currentTip)) {
            return;
        }
        LogEntry entry = changes.read(currentTip);
        templateCache.clear();
        currentVersion = currentTip;
        tipLastModified = entry.time;
        tip = currentVersion.toString();
        currentManifest = store.manifest().read(entry.manifest);
        Logger log = Logger.getLogger("de.mobizcorp.femtocms.engine");
        log.info("loaded manifest, " + currentManifest.size() + " entries for "
                + currentTip + " (" + entry.user + ")");
        dirty = false;
    }

    private boolean isInManifest(Text path) throws IOException {
        initManifest();
        return getManifest().get(path) != null;
    }

    private HashMap<Text, Entry> getManifest() throws IOException {
        if (dirty) {
            initManifest();
        }
        return currentManifest;
    }

    @Override
    protected void configureTransformer(String style, Transformer result) {
        super.configureTransformer(style, result);
        Entry entry = currentManifest.get(style);
        if (entry != null) {
            result.setParameter("femtocms-base", base.getPath());
            result.setParameter("femtocms-sha1", entry.version.toString());
        }
        result.setParameter("femtocms-tip", tip);
    }

    public String modified() {
        return tipDateFormat.format(new Date(tipLastModified));
    }

    public void refresh() {
        // force manifest reload
        this.dirty = true;
    }

    @Override
    protected Templates newTemplates(String style) throws TransformerException {
        Templates templates = (Templates) templateCache.get(style);
        if (templates != null) {
            return templates;
        }
        templates = super.newTemplates(style);
        templateCache.put(style, templates);
        return templates;
    }

}
