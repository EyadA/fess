/*
 * Copyright 2012-2018 CodeLibs Project and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.codelibs.fess.mylasta.direction;

import static org.codelibs.core.stream.StreamUtil.split;
import static org.codelibs.core.stream.StreamUtil.stream;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;

import org.codelibs.core.exception.ClassNotFoundRuntimeException;
import org.codelibs.core.lang.StringUtil;
import org.codelibs.core.misc.Pair;
import org.codelibs.core.misc.Tuple3;
import org.codelibs.fess.Constants;
import org.codelibs.fess.exception.FessSystemException;
import org.codelibs.fess.helper.PermissionHelper;
import org.codelibs.fess.mylasta.action.FessUserBean;
import org.codelibs.fess.taglib.FessFunctions;
import org.codelibs.fess.util.ComponentUtil;
import org.codelibs.fess.util.PrunedTag;
import org.dbflute.optional.OptionalThing;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.lastaflute.job.LaJob;
import org.lastaflute.job.subsidiary.JobConcurrentExec;
import org.lastaflute.web.validation.RequiredValidator;
import org.lastaflute.web.validation.theme.typed.DoubleTypeValidator;
import org.lastaflute.web.validation.theme.typed.FloatTypeValidator;
import org.lastaflute.web.validation.theme.typed.IntegerTypeValidator;
import org.lastaflute.web.validation.theme.typed.LongTypeValidator;

public interface FessProp {

    public static final String LOGGING_SEARCH_DOCS_FIELDS = "loggingSearchDocsFields";

    public static final String API_SEARCH_ACCEPT_REFERERS = "apiSearchAcceptReferers";

    public static final String QUERY_GSA_RESPONSE_FIELDS = "queryGsaResponseFields";

    public static final String THUMBNAIL_HTML_IMAGE_EXCLUDE_EXTENSIONS = "ThumbnailHtmlImageExcludeExtensions";

    public static final String VIRTUAL_HOST_VALUE = "VirtualHostValue";

    public static final String QUERY_DEFAULT_LANGUAGES = "queryDefaultLanguages";

    public static final String HTML_PROXY = "httpProxy";

    public static final String CRAWLER_FAILURE_URL_STATUS_CODES = "crawlerFailureUrlStatusCodes";

    public static final String VIRTUAL_HOST_HEADERS = "virtualHostHeaders";

    public static final String QUERY_COLLAPSE_INNER_HITS_SORTS = "queryCollapseInnerHitsSorts";

    public static final String USER_CODE_PATTERN = "userCodePattern";

    public static final String API_ADMIN_ACCESS_PERMISSION_SET = "apiAdminAccessPermissionSet";

    public static final String CRAWLER_DOCUMENT_SPACE_CHARS = "crawlerDocumentSpaceChars";

    public static final String CRAWLER_DOCUMENT_FULLSTOP_CHARS = "crawlerDocumentFullstopChars";

    public static final String INDEX_ADMIN_ARRAY_FIELD_SET = "indexAdminArrayFieldSet";

    public static final String INDEX_ADMIN_DATE_FIELD_SET = "indexAdminDateFieldSet";

    public static final String INDEX_ADMIN_INTEGER_FIELD_SET = "indexAdminIntegerFieldSet";

    public static final String INDEX_ADMIN_LONG_FIELD_SET = "indexAdminLongFieldSet";

    public static final String INDEX_ADMIN_FLOAT_FIELD_SET = "indexAdminFloatFieldSet";

    public static final String INDEX_ADMIN_DOUBLE_FIELD_SET = "indexAdminDoubleFieldSet";

    public static final String OIC_DEFAULT_ROLES = "oicDefaultRoles";

    public static final String OIC_DEFAULT_GROUPS = "oicDefaultGroups";

    public static final String AUTHENTICATION_ADMIN_ROLES = "authenticationAdminRoles";

    public static final String SEARCH_GUEST_PERMISSION_LIST = "searchGuestPermissionList";

    public static final String SUGGEST_SEARCH_LOG_PERMISSIONS = "suggestSearchLogPermissions";

    public static final String GROUP_VALUE_PREFIX = "group:";

    public static final String ROLE_VALUE_PREFIX = "role:";

    public static final String DEFAULT_SORT_VALUES = "defaultSortValues";

    public static final String DEFAULT_LABEL_VALUES = "defaultLabelValues";

    public static final String VIRTUAL_HOST_VALUES = "virtualHostValues";

    public static final String QUERY_LANGUAGE_MAPPING = "queryLanguageMapping";

    public static final String CRAWLER_METADATA_NAME_MAPPING = "crawlerMetadataNameMapping";

    public static final String CRAWLER_METADATA_CONTENT_EXCLUDES = "crawlerMetadataContentExcludes";

    public static final Map<String, Object> propMap = new ConcurrentHashMap<>();

    //
    // system.properties
    //

    public default void storeSystemProperties() {
        ComponentUtil.getSystemProperties().store();
    }

    public default String getSystemProperty(final String key) {
        return ComponentUtil.getSystemProperties().getProperty(key);
    }

    public default String getSystemProperty(final String key, final String defaultValue) {
        return ComponentUtil.getSystemProperties().getProperty(key, defaultValue);
    }

    public default void setSystemProperty(final String key, final String value) {
        if (value != null) {
            ComponentUtil.getSystemProperties().setProperty(key, value);
        } else {
            ComponentUtil.getSystemProperties().remove(key);
        }
    }

    public default boolean getSystemPropertyAsBoolean(final String key, final boolean defaultValue) {
        return Constants.TRUE.equalsIgnoreCase(getSystemProperty(key, defaultValue ? Constants.TRUE : Constants.FALSE));
    }

    public default void setSystemPropertyAsBoolean(final String key, final boolean value) {
        setSystemProperty(key, value ? Constants.TRUE : Constants.FALSE);
    }

    public default int getSystemPropertyAsInt(final String key, final int defaultValue) {
        final String value = getSystemProperty(key);
        if (value != null) {
            try {
                return Integer.valueOf(value);
            } catch (final NumberFormatException e) {
                // ignore
            }
        }
        return defaultValue;
    }

    public default void setSystemPropertyAsInt(final String key, final int value) {
        setSystemProperty(key, Integer.toString(value));
    }

    public default boolean isWebDesignEditorEnabled() {
        return getSystemPropertyAsBoolean(Constants.WEB_DESIGN_EDITOR_PROPERTY, true);
    }

    public default boolean isSearchFileProxyEnabled() {
        return getSystemPropertyAsBoolean(Constants.SEARCH_FILE_PROXY_PROPERTY, true);
    }

    public default boolean isBrowserLocaleForSearchUsed() {
        return getSystemPropertyAsBoolean(Constants.USE_BROWSER_LOCALE_FOR_SEARCH_PROPERTY, false);
    }

    public default String[] getDefaultSortValues(final OptionalThing<FessUserBean> userBean) {
        @SuppressWarnings("unchecked")
        List<Pair<String, String>> list = (List<Pair<String, String>>) propMap.get(DEFAULT_SORT_VALUES);
        if (list == null) {
            final String value = getSystemProperty(Constants.DEFAULT_SORT_VALUE_PROPERTY);
            if (StringUtil.isBlank(value)) {
                list = Collections.emptyList();
            } else {
                final Set<String> keySet = new HashSet<>();
                list = split(value, "\n").get(stream -> stream.filter(StringUtil::isNotBlank).map(s -> {
                    final String[] pair = s.split("=");
                    if (pair.length == 1) {
                        return new Pair<>(StringUtil.EMPTY, pair[0].trim());
                    } else if (pair.length == 2) {
                        String sortValue = pair[1].trim();
                        if (StringUtil.isBlank(sortValue) || "score".equals(sortValue)) {
                            sortValue = "score.desc";
                        }
                        return new Pair<>(pair[0].trim(), sortValue);
                    }
                    return null;
                }).filter(o -> o != null && keySet.add(o.getFirst())).collect(Collectors.toList()));
            }
            propMap.put(DEFAULT_SORT_VALUES, list);
        }
        return list
                .stream()
                .map(p -> {
                    final String key = p.getFirst();
                    if (StringUtil.isEmpty(key)) {
                        return p.getSecond();
                    }
                    if (userBean.map(
                            user -> stream(user.getRoles()).get(stream -> stream.anyMatch(s -> key.equals(ROLE_VALUE_PREFIX + s)))
                                    || stream(user.getGroups()).get(stream -> stream.anyMatch(s -> key.equals(GROUP_VALUE_PREFIX + s))))
                            .orElse(false)) {
                        return p.getSecond();
                    }
                    return null;
                }).filter(StringUtil::isNotBlank).toArray(n -> new String[n]);
    }

    public default void setDefaultSortValue(final String value) {
        setSystemProperty(Constants.DEFAULT_SORT_VALUE_PROPERTY, value);
        propMap.remove(DEFAULT_SORT_VALUES);
    }

    public default String getDefaultSortValue() {
        return getSystemProperty(Constants.DEFAULT_SORT_VALUE_PROPERTY, StringUtil.EMPTY);
    }

    public default String[] getDefaultLabelValues(final OptionalThing<FessUserBean> userBean) {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> map = (Map<String, List<String>>) propMap.get(DEFAULT_LABEL_VALUES);
        if (map == null) {
            final String value = getSystemProperty(Constants.DEFAULT_LABEL_VALUE_PROPERTY);
            if (StringUtil.isBlank(value)) {
                map = Collections.emptyMap();
            } else {
                final Set<String> keySet = new HashSet<>();
                map =
                        split(value, "\n").get(
                                stream -> stream
                                        .filter(StringUtil::isNotBlank)
                                        .map(s -> {
                                            final String[] pair = s.split("=");
                                            if (pair.length == 1) {
                                                return new Pair<>(StringUtil.EMPTY, pair[0].trim());
                                            } else if (pair.length == 2) {
                                                return new Pair<>(pair[0].trim(), pair[1].trim());
                                            }
                                            return null;
                                        })
                                        .filter(o -> o != null && keySet.add(o.getFirst()))
                                        .collect(HashMap<String, List<String>>::new,
                                                (m, d) -> m.put(d.getFirst(), Arrays.asList(d.getSecond().split(","))),
                                                (m, u) -> m.putAll(u)));
            }
            propMap.put(DEFAULT_LABEL_VALUES, map);
        }
        return map
                .entrySet()
                .stream()
                .flatMap(
                        e -> {
                            final String key = e.getKey();
                            if (StringUtil.isEmpty(key)) {
                                return e.getValue().stream();
                            }
                            if (userBean.map(
                                    user -> stream(user.getRoles()).get(stream -> stream.anyMatch(s -> key.equals(ROLE_VALUE_PREFIX + s)))
                                            || stream(user.getGroups()).get(
                                                    stream -> stream.anyMatch(s -> key.equals(GROUP_VALUE_PREFIX + s)))).orElse(false)) {
                                return e.getValue().stream();
                            }
                            return Stream.empty();
                        }).filter(StringUtil::isNotBlank).toArray(n -> new String[n]);
    }

    public default void setDefaultLabelValue(final String value) {
        setSystemProperty(Constants.DEFAULT_LABEL_VALUE_PROPERTY, value);
        propMap.remove(DEFAULT_LABEL_VALUES);
    }

    public default String getDefaultLabelValue() {
        return getSystemProperty(Constants.DEFAULT_LABEL_VALUE_PROPERTY, StringUtil.EMPTY);
    }

    public default void setVirtualHostValue(final String value) {
        setSystemProperty(Constants.VIRTUAL_HOST_VALUE_PROPERTY, value);
        propMap.remove(VIRTUAL_HOST_HEADERS);
    }

    public default String getVirtualHostValue() {
        return getSystemProperty(Constants.VIRTUAL_HOST_VALUE_PROPERTY, getVirtualHostHeaders());
    }

    public default void setLoginRequired(final boolean value) {
        setSystemPropertyAsBoolean(Constants.LOGIN_REQUIRED_PROPERTY, value);
    }

    public default boolean isLoginRequired() {
        return getSystemPropertyAsBoolean(Constants.LOGIN_REQUIRED_PROPERTY, false);
    }

    public default void setResultCollapsed(final boolean value) {
        setSystemPropertyAsBoolean(Constants.RESULT_COLLAPSED_PROPERTY, value);
    }

    public default boolean isResultCollapsed() {
        return getSystemPropertyAsBoolean(Constants.RESULT_COLLAPSED_PROPERTY, false);
    }

    public default void setLoginLinkEnabled(final boolean value) {
        setSystemPropertyAsBoolean(Constants.LOGIN_LINK_ENALBED_PROPERTY, value);
    }

    public default boolean isLoginLinkEnabled() {
        return getSystemPropertyAsBoolean(Constants.LOGIN_LINK_ENALBED_PROPERTY, true);
    }

    public default void setThumbnailEnabled(final boolean value) {
        setSystemPropertyAsBoolean(Constants.THUMBNAIL_ENALBED_PROPERTY, value);
    }

    public default boolean isThumbnailEnabled() {
        return getSystemPropertyAsBoolean(Constants.THUMBNAIL_ENALBED_PROPERTY, false);
    }

    public default void setIncrementalCrawling(final boolean value) {
        setSystemPropertyAsBoolean(Constants.INCREMENTAL_CRAWLING_PROPERTY, value);
    }

    public default boolean isIncrementalCrawling() {
        return getSystemPropertyAsBoolean(Constants.INCREMENTAL_CRAWLING_PROPERTY, true);
    }

    public default void setDayForCleanup(final int value) {
        setSystemPropertyAsInt(Constants.DAY_FOR_CLEANUP_PROPERTY, value);
    }

    public default int getDayForCleanup() {
        return getSystemPropertyAsInt(Constants.DAY_FOR_CLEANUP_PROPERTY, Constants.DEFAULT_DAY_FOR_CLEANUP);
    }

    public default void setCrawlingThreadCount(final int value) {
        setSystemPropertyAsInt(Constants.CRAWLING_THREAD_COUNT_PROPERTY, value);
    }

    public default int getCrawlingThreadCount() {
        return getSystemPropertyAsInt(Constants.CRAWLING_THREAD_COUNT_PROPERTY, 5);
    }

    public default void setSearchLog(final boolean value) {
        setSystemPropertyAsBoolean(Constants.SEARCH_LOG_PROPERTY, value);
    }

    public default boolean isSearchLog() {
        return getSystemPropertyAsBoolean(Constants.SEARCH_LOG_PROPERTY, true);
    }

    public default void setUserInfo(final boolean value) {
        setSystemPropertyAsBoolean(Constants.USER_INFO_PROPERTY, value);
    }

    public default boolean isUserInfo() {
        return getSystemPropertyAsBoolean(Constants.USER_INFO_PROPERTY, true);
    }

    public default void setUserFavorite(final boolean value) {
        setSystemPropertyAsBoolean(Constants.USER_FAVORITE_PROPERTY, value);
    }

    public default boolean isUserFavorite() {
        return getSystemPropertyAsBoolean(Constants.USER_FAVORITE_PROPERTY, false);
    }

    public default void setWebApiJson(final boolean value) {
        setSystemPropertyAsBoolean(Constants.WEB_API_JSON_PROPERTY, value);
    }

    public default boolean isWebApiJson() {
        return getSystemPropertyAsBoolean(Constants.WEB_API_JSON_PROPERTY, true);
    }

    public default boolean isWebApiGsa() {
        return getSystemPropertyAsBoolean(Constants.WEB_API_GSA_PROPERTY, false);
    }

    public default void setAppendQueryParameter(final boolean value) {
        setSystemPropertyAsBoolean(Constants.APPEND_QUERY_PARAMETER_PROPERTY, value);
    }

    public default boolean isAppendQueryParameter() {
        return getSystemPropertyAsBoolean(Constants.APPEND_QUERY_PARAMETER_PROPERTY, false);
    }

    public default void setIgnoreFailureType(final String value) {
        setSystemProperty(Constants.IGNORE_FAILURE_TYPE_PROPERTY, value);
    }

    public default String getIgnoreFailureType() {
        return getSystemProperty(Constants.IGNORE_FAILURE_TYPE_PROPERTY, Constants.DEFAULT_IGNORE_FAILURE_TYPE);
    }

    public default void setFailureCountThreshold(final int value) {
        setSystemPropertyAsInt(Constants.FAILURE_COUNT_THRESHOLD_PROPERTY, value);
    }

    public default int getFailureCountThreshold() {
        return getSystemPropertyAsInt(Constants.FAILURE_COUNT_THRESHOLD_PROPERTY, Constants.DEFAULT_FAILURE_COUNT);
    }

    public default void setWebApiPopularWord(final boolean value) {
        setSystemPropertyAsBoolean(Constants.WEB_API_POPULAR_WORD_PROPERTY, value);
    }

    public default boolean isWebApiPopularWord() {
        return getSystemPropertyAsBoolean(Constants.WEB_API_POPULAR_WORD_PROPERTY, true);
    }

    public default void setCsvFileEncoding(final String value) {
        setSystemProperty(Constants.CSV_FILE_ENCODING_PROPERTY, value);
    }

    public default String getCsvFileEncoding() {
        return getSystemProperty(Constants.CSV_FILE_ENCODING_PROPERTY, Constants.UTF_8);
    }

    public default void setPurgeSearchLogDay(final int value) {
        setSystemPropertyAsInt(Constants.PURGE_SEARCH_LOG_DAY_PROPERTY, value);
    }

    public default int getPurgeSearchLogDay() {
        return getSystemPropertyAsInt(Constants.PURGE_SEARCH_LOG_DAY_PROPERTY, Integer.parseInt(Constants.DEFAULT_PURGE_DAY));
    }

    public default void setPurgeJobLogDay(final int value) {
        setSystemPropertyAsInt(Constants.PURGE_JOB_LOG_DAY_PROPERTY, value);
    }

    public default int getPurgeJobLogDay() {
        return getSystemPropertyAsInt(Constants.PURGE_JOB_LOG_DAY_PROPERTY, Integer.parseInt(Constants.DEFAULT_PURGE_DAY));
    }

    public default void setPurgeUserInfoDay(final int value) {
        setSystemPropertyAsInt(Constants.PURGE_USER_INFO_DAY_PROPERTY, value);
    }

    public default int getPurgeUserInfoDay() {
        return getSystemPropertyAsInt(Constants.PURGE_USER_INFO_DAY_PROPERTY, Integer.parseInt(Constants.DEFAULT_PURGE_DAY));
    }

    public default void setPurgeByBots(final String value) {
        setSystemProperty(Constants.PURGE_BY_BOTS_PROPERTY, value);
    }

    public default String getPurgeByBots() {
        return getSystemProperty(Constants.PURGE_BY_BOTS_PROPERTY, Constants.DEFAULT_PURGE_BY_BOTS);
    }

    public default void setNotificationTo(final String value) {
        setSystemProperty(Constants.NOTIFICATION_TO_PROPERTY, value);
    }

    public default String getNotificationTo() {
        return getSystemProperty(Constants.NOTIFICATION_TO_PROPERTY, StringUtil.EMPTY);
    }

    public default void setSuggestSearchLog(final boolean value) {
        setSystemPropertyAsBoolean(Constants.SUGGEST_SEARCH_LOG_PROPERTY, value);
    }

    public default boolean isSuggestSearchLog() {
        return getSystemPropertyAsBoolean(Constants.SUGGEST_SEARCH_LOG_PROPERTY, true);
    }

    public default void setSuggestDocuments(final boolean value) {
        setSystemPropertyAsBoolean(Constants.SUGGEST_DOCUMENTS_PROPERTY, value);
    }

    public default boolean isSuggestDocuments() {
        return getSystemPropertyAsBoolean(Constants.SUGGEST_DOCUMENTS_PROPERTY, true);
    }

    public default void setPurgeSuggestSearchLogDay(final int value) {
        setSystemPropertyAsInt(Constants.PURGE_SUGGEST_SEARCH_LOG_DAY_PROPERTY, value);
    }

    public default int getPurgeSuggestSearchLogDay() {
        return getSystemPropertyAsInt(Constants.PURGE_SUGGEST_SEARCH_LOG_DAY_PROPERTY,
                Integer.parseInt(Constants.DEFAULT_SUGGEST_PURGE_DAY));
    }

    public default void setLdapInitialContextFactory(final String value) {
        setSystemProperty(Constants.LDAP_INITIAL_CONTEXT_FACTORY, value);
    }

    public default String getLdapInitialContextFactory() {
        return getSystemProperty(Constants.LDAP_INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    }

    public default void setLdapSecurityAuthentication(final String value) {
        setSystemProperty(Constants.LDAP_SECURITY_AUTHENTICATION, value);
    }

    public default String getLdapSecurityAuthentication() {
        return getSystemProperty(Constants.LDAP_SECURITY_AUTHENTICATION, "simple");
    }

    public default void setLdapProviderUrl(final String value) {
        setSystemProperty(Constants.LDAP_PROVIDER_URL, value);
    }

    public default String getLdapProviderUrl() {
        return getSystemProperty(Constants.LDAP_PROVIDER_URL);
    }

    public default void setLdapSecurityPrincipal(final String value) {
        setSystemProperty(Constants.LDAP_SECURITY_PRINCIPAL, value);
    }

    public default String getLdapMemberofAttribute() {
        return getSystemProperty(Constants.LDAP_MEMBEROF_ATTRIBUTE, "memberOf");
    }

    public default void setLdapMemberofAttribute(final String value) {
        setSystemProperty(Constants.LDAP_MEMBEROF_ATTRIBUTE, value);
    }

    Integer getLdapMaxUsernameLengthAsInteger();

    public default String getLdapSecurityPrincipal(final String username) {
        final String value;
        final int maxLength = getLdapMaxUsernameLengthAsInteger().intValue();
        if (username == null) {
            value = StringUtil.EMPTY;
        } else if (maxLength >= 0 && username.length() > maxLength) {
            value = username.substring(0, maxLength);
        } else {
            value = username;
        }
        return String.format(getSystemProperty(Constants.LDAP_SECURITY_PRINCIPAL, StringUtil.EMPTY), value);
    }

    public default String getLdapSecurityPrincipal() {
        return getSystemProperty(Constants.LDAP_SECURITY_PRINCIPAL);
    }

    public default void setLdapAdminSecurityPrincipal(final String value) {
        setSystemProperty(Constants.LDAP_ADMIN_SECURITY_PRINCIPAL, value);
    }

    public default String getLdapAdminSecurityPrincipal() {
        return getSystemProperty(Constants.LDAP_ADMIN_SECURITY_PRINCIPAL);
    }

    public default void setLdapAdminSecurityCredentials(final String value) {
        setSystemProperty(Constants.LDAP_ADMIN_SECURITY_CREDENTIALS,
                Constants.CIPHER_PREFIX + ComponentUtil.getPrimaryCipher().encrypt(value));
    }

    public default String getLdapAdminSecurityCredentials() {
        final String value = getSystemProperty(Constants.LDAP_ADMIN_SECURITY_CREDENTIALS);
        if (StringUtil.isNotBlank(value) && value.startsWith(Constants.CIPHER_PREFIX)) {
            return ComponentUtil.getPrimaryCipher().decrypt(value.substring(Constants.CIPHER_PREFIX.length()));
        }
        return value;
    }

    public default void setLdapBaseDn(final String value) {
        setSystemProperty(Constants.LDAP_BASE_DN, value);
    }

    public default String getLdapBaseDn() {
        return getSystemProperty(Constants.LDAP_BASE_DN);
    }

    public default void setLdapAccountFilter(final String value) {
        setSystemProperty(Constants.LDAP_ACCOUNT_FILTER, value);
    }

    public default String getLdapAccountFilter() {
        return getSystemProperty(Constants.LDAP_ACCOUNT_FILTER);
    }

    public default void setNotificationLogin(final String value) {
        setSystemProperty(Constants.NOTIFICATION_LOGIN, value);
    }

    public default String getNotificationLogin() {
        return getSystemProperty(Constants.NOTIFICATION_LOGIN, StringUtil.EMPTY);
    }

    public default String getNotificationAdvanceSearch() {
        return getSystemProperty(Constants.NOTIFICATION_ADVANCE_SEARCH, StringUtil.EMPTY);
    }

    public default void setNotificationSearchTop(final String value) {
        setSystemProperty(Constants.NOTIFICATION_SEARCH_TOP, value);
    }

    public default String getNotificationSearchTop() {
        return getSystemProperty(Constants.NOTIFICATION_SEARCH_TOP, StringUtil.EMPTY);
    }

    public default String getUserAgentName() {
        return getSystemProperty(Constants.CRAWLING_USER_AGENT_PROPERTY, "Mozilla/5.0 (compatible; Fess/"
                + ComponentUtil.getSystemHelper().getProductVersion() + "; +http://fess.codelibs.org/bot.html)");
    }

    //
    // fess_*.properties
    //

    String getAuthenticationAdminRoles();

    public default String[] getAuthenticationAdminRolesAsArray() {
        String[] roles = (String[]) propMap.get(AUTHENTICATION_ADMIN_ROLES);
        if (roles == null) {
            roles = getAuthenticationAdminRoles().split(",");
            propMap.put(AUTHENTICATION_ADMIN_ROLES, roles);
        }
        return roles;
    }

    String getJvmCrawlerOptions();

    public default String[] getJvmCrawlerOptionsAsArray() {
        return getJvmCrawlerOptions().split("\n");
    }

    String getJvmSuggestOptions();

    public default String[] getJvmSuggestOptionsAsArray() {
        return getJvmSuggestOptions().split("\n");
    }

    String getCrawlerDocumentHtmlPrunedTags();

    public default PrunedTag[] getCrawlerDocumentHtmlPrunedTagsAsArray() {
        PrunedTag[] tags = (PrunedTag[]) propMap.get("crawlerDocumentHtmlPrunedTags");
        if (tags == null) {
            tags = split(getCrawlerDocumentHtmlPrunedTags(), ",").get(stream -> stream.filter(StringUtil::isNotBlank).map(v -> {
                final Pattern pattern = Pattern.compile("(\\w+)(\\[[^\\]]+\\])?(\\.\\w+)?(#\\w+)?");
                final Matcher matcher = pattern.matcher(v.trim());
                if (matcher.matches()) {
                    final PrunedTag tag = new PrunedTag(matcher.group(1));
                    if (matcher.group(2) != null) {
                        final String attrPair = matcher.group(2).substring(1, matcher.group(2).length() - 1);
                        final Matcher equalMatcher = Pattern.compile("([\\w\\-]+)=(\\S+)").matcher(attrPair);
                        if (equalMatcher.matches()) {
                            tag.setAttr(equalMatcher.group(1), equalMatcher.group(2));
                        }
                    }
                    if (matcher.group(3) != null) {
                        tag.setCss(matcher.group(3).substring(1));
                    }
                    if (matcher.group(4) != null) {
                        tag.setId(matcher.group(4).substring(1));
                    }
                    return tag;
                }
                throw new FessSystemException("Invalid pruned tag: " + v);
            }).toArray(n -> new PrunedTag[n]));
            propMap.put("crawlerDocumentHtmlPrunedTags", tags);
        }
        return tags;
    }

    String getCrawlerDocumentCacheHtmlMimetypes();

    public default boolean isHtmlMimetypeForCache(final String mimetype) {
        final String[] mimetypes = getCrawlerDocumentCacheHtmlMimetypes().split(",");
        if (mimetypes.length == 1 && StringUtil.isBlank(mimetypes[0])) {
            return true;
        }
        return stream(mimetypes).get(stream -> stream.anyMatch(s -> s.equalsIgnoreCase(mimetype)));
    }

    String getCrawlerDocumentCacheSupportedMimetypes();

    public default boolean isSupportedDocumentCacheMimetypes(final String mimetype) {
        final String[] mimetypes = getCrawlerDocumentCacheSupportedMimetypes().split(",");
        if (mimetypes.length == 1 && StringUtil.isBlank(mimetypes[0])) {
            return true;
        }
        return stream(mimetypes).get(stream -> stream.anyMatch(s -> s.equalsIgnoreCase(mimetype)));
    }

    String getIndexerClickCountEnabled();

    public default boolean getIndexerClickCountEnabledAsBoolean() {
        return Constants.TRUE.equalsIgnoreCase(getIndexerClickCountEnabled());
    }

    String getIndexerFavoriteCountEnabled();

    public default boolean getIndexerFavoriteCountEnabledAsBoolean() {
        return Constants.TRUE.equalsIgnoreCase(getIndexerFavoriteCountEnabled());
    }

    String getApiAccessTokenRequired();

    public default boolean getApiAccessTokenRequiredAsBoolean() {
        return Constants.TRUE.equalsIgnoreCase(getApiAccessTokenRequired());
    }

    String getIndexerThreadDumpEnabled();

    public default boolean getIndexerThreadDumpEnabledAsBoolean() {
        return Constants.TRUE.equalsIgnoreCase(getIndexerThreadDumpEnabled());
    }

    String getIndexBackupTargets();

    String getIndexBackupLogTargets();

    public default String[] getIndexBackupAllTargets() {
        return split(getIndexBackupTargets() + "," + getIndexBackupLogTargets(), ",").get(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).toArray(n -> new String[n]));
    }

    String getJobSystemJobIds();

    public default boolean isSystemJobId(final String id) {
        if (StringUtil.isBlank(getJobSystemJobIds())) {
            return false;
        }
        return split(getJobSystemJobIds(), ",").get(stream -> stream.anyMatch(s -> s.equals(id)));
    }

    String getSmbAvailableSidTypes();

    public default boolean isAvailableSmbSidType(final int sidType) {
        if (StringUtil.isBlank(getSmbAvailableSidTypes())) {
            return false;
        }
        final String value = Integer.toString(sidType);
        return split(getSmbAvailableSidTypes(), ",").get(stream -> stream.anyMatch(s -> s.equals(value)));
    }

    String getSupportedLanguages();

    public default String[] getSupportedLanguagesAsArray() {
        return split(getSupportedLanguages(), ",").get(stream -> stream.filter(StringUtil::isNotBlank).toArray(n -> new String[n]));
    }

    String getOnlineHelpSupportedLangs();

    public default boolean isOnlineHelpSupportedLang(final String lang) {
        if (StringUtil.isBlank(getOnlineHelpSupportedLangs())) {
            return false;
        }
        return split(getOnlineHelpSupportedLangs(), ",").get(stream -> stream.filter(StringUtil::isNotBlank).anyMatch(s -> s.equals(lang)));
    }

    String getSupportedUploadedJsExtentions();

    public default String[] getSupportedUploadedJsExtentionsAsArray() {
        return split(getSupportedUploadedJsExtentions(), ",").get(
                stream -> stream.filter(StringUtil::isNotBlank).toArray(n -> new String[n]));
    }

    String getSupportedUploadedCssExtentions();

    public default String[] getSupportedUploadedCssExtentionsAsArray() {
        return split(getSupportedUploadedCssExtentions(), ",").get(
                stream -> stream.filter(StringUtil::isNotBlank).toArray(n -> new String[n]));
    }

    String getSupportedUploadedMediaExtentions();

    public default String[] getSupportedUploadedMediaExtentionsAsArray() {
        return split(getSupportedUploadedMediaExtentions(), ",").get(
                stream -> stream.filter(StringUtil::isNotBlank).toArray(n -> new String[n]));
    }

    String getJobTemplateTitleWeb();

    String getJobTemplateTitleFile();

    String getJobTemplateTitleData();

    public default String getJobTemplateTitle(final String type) {
        if (Constants.WEB_CRAWLER_TYPE.equals(type)) {
            return getJobTemplateTitleWeb();
        } else if (Constants.FILE_CRAWLER_TYPE.equals(type)) {
            return getJobTemplateTitleFile();
        } else if (Constants.DATA_CRAWLER_TYPE.equals(type)) {
            return getJobTemplateTitleData();
        }
        return "None";
    }

    String getSchedulerJobClass();

    public default Class<? extends LaJob> getSchedulerJobClassAsClass() {
        try {
            @SuppressWarnings("unchecked")
            final Class<? extends LaJob> clazz = (Class<? extends LaJob>) Class.forName(getSchedulerJobClass());
            return clazz;
        } catch (final ClassNotFoundException e) {
            throw new ClassNotFoundRuntimeException(e);
        }
    }

    String getSchedulerConcurrentExecMode();

    public default JobConcurrentExec getSchedulerConcurrentExecModeAsEnum() {
        return JobConcurrentExec.valueOf(getSchedulerConcurrentExecMode());
    }

    String getCrawlerMetadataContentExcludes();

    public default boolean isCrawlerMetadataContentIncluded(final String name) {
        Pattern[] patterns = (Pattern[]) propMap.get(CRAWLER_METADATA_CONTENT_EXCLUDES);
        if (patterns == null) {
            patterns =
                    split(getCrawlerMetadataContentExcludes(), ",").get(
                            stream -> stream.filter(StringUtil::isNotBlank).map(v -> Pattern.compile(v)).toArray(n -> new Pattern[n]));
            propMap.put(CRAWLER_METADATA_CONTENT_EXCLUDES, patterns);
        }
        return !stream(patterns).get(stream -> stream.anyMatch(p -> p.matcher(name).matches()));
    }

    String getCrawlerMetadataNameMapping();

    public default Pair<String, String> getCrawlerMetadataNameMapping(final String name) {
        @SuppressWarnings("unchecked")
        Map<String, Pair<String, String>> params = (Map<String, Pair<String, String>>) propMap.get(CRAWLER_METADATA_NAME_MAPPING);
        if (params == null) {
            params = split(getCrawlerMetadataNameMapping(), "\n").get(stream -> stream.filter(StringUtil::isNotBlank).map(v -> {
                final String[] values = v.split("=");
                if (values.length == 2) {
                    final String[] subValues = values[1].split(":");
                    if (subValues.length == 2) {
                        return new Tuple3<>(values[0], subValues[0], subValues[1]);
                    } else {
                        return new Tuple3<>(values[0], values[1], Constants.MAPPING_TYPE_ARRAY);
                    }
                }
                return null;
            }).collect(Collectors.toMap(Tuple3::getValue1, d -> new Pair<>(d.getValue2(), d.getValue3()))));
            propMap.put(CRAWLER_METADATA_NAME_MAPPING, params);
        }
        return params.get(name);
    }

    String getSuggestPopularWordFields();

    public default String[] getSuggestPopularWordFieldsAsArray() {
        return split(getSuggestPopularWordFields(), "\n").get(stream -> stream.filter(StringUtil::isNotBlank).toArray(n -> new String[n]));
    }

    String getSuggestPopularWordTags();

    public default String[] getSuggestPopularWordTagsAsArray() {
        return split(getSuggestPopularWordTags(), "\n").get(stream -> stream.filter(StringUtil::isNotBlank).toArray(n -> new String[n]));
    }

    String getSuggestPopularWordExcludes();

    public default String[] getSuggestPopularWordExcludesAsArray() {
        return split(getSuggestPopularWordExcludes(), "\n")
                .get(stream -> stream.filter(StringUtil::isNotBlank).toArray(n -> new String[n]));
    }

    String getQueryReplaceTermWithPrefixQuery();

    public default boolean getQueryReplaceTermWithPrefixQueryAsBoolean() {
        return Boolean.valueOf(getQueryReplaceTermWithPrefixQuery());
    }

    String getQueryDefaultLanguages();

    String getQueryLanguageMapping();

    public default String[] normalizeQueryLanguages(final String[] langs) {
        @SuppressWarnings("unchecked")
        Map<String, String> params = (Map<String, String>) propMap.get(QUERY_LANGUAGE_MAPPING);
        if (params == null) {
            params = stream(getQueryLanguageMapping().split("\n")).get(stream -> stream.filter(StringUtil::isNotBlank).map(v -> {
                final String[] values = v.split("=");
                if (values.length == 2) {
                    return new Pair<>(values[0], values[1]);
                }
                return null;
            }).collect(Collectors.toMap(Pair::getFirst, d -> d.getSecond())));
            propMap.put(QUERY_LANGUAGE_MAPPING, params);
        }
        final Map<String, String> mapping = params;
        return stream(langs).get(stream -> stream.map(s -> {
            if (StringUtil.isBlank(s)) {
                return null;
            }
            final String lang1 = mapping.get(s);
            if (lang1 != null) {
                return lang1;
            }
            final String lang2 = mapping.get(s.split("[\\-_]")[0]);
            if (lang2 != null) {
                return lang2;
            }
            return null;
        }).filter(StringUtil::isNotBlank).distinct().toArray(n -> new String[n]));
    }

    public default String[] getQueryLanguages(final Enumeration<Locale> locales, final String[] requestLangs) {
        // requestLangs > default > browser
        if (StringUtil.isNotBlank(getQueryDefaultLanguages())) {
            String[] langs = (String[]) propMap.get(QUERY_DEFAULT_LANGUAGES);
            if (langs == null) {
                langs = split(getQueryDefaultLanguages(), ",").get(stream -> stream.map(s -> s.trim()).toArray(n -> new String[n]));
                propMap.put(QUERY_DEFAULT_LANGUAGES, langs);

            }
            return normalizeQueryLanguages(langs);
        }

        if (requestLangs != null && requestLangs.length != 0) {
            return normalizeQueryLanguages(requestLangs);
        }

        if (locales == null) {
            return StringUtil.EMPTY_STRINGS;
        }

        return normalizeQueryLanguages(Collections.list(locales).stream().map(locale -> {
            final String language = locale.getLanguage();
            final String country = locale.getCountry();
            if (StringUtil.isNotBlank(language)) {
                if (StringUtil.isNotBlank(country)) {
                    return language.toLowerCase(Locale.ROOT) + "-" + country.toLowerCase(Locale.ROOT);
                }
                return language.toLowerCase(Locale.ROOT);
            }
            return null;
        }).toArray(n -> new String[n]));
    }

    String getSupportedUploadedFiles();

    public default boolean isSupportedUploadedFile(final String name) {
        return split(getSuggestPopularWordExcludes(), ",").get(
                stream -> stream.filter(StringUtil::isNotBlank).anyMatch(s -> s.equals(name)));
    }

    String getLdapAdminUserObjectClasses();

    public default Attribute getLdapAdminUserObjectClassAttribute() {
        final Attribute oc = new BasicAttribute("objectClass");
        split(getLdapAdminUserObjectClasses(), ",").of(stream -> stream.filter(StringUtil::isNotBlank).forEach(s -> oc.add(s.trim())));
        return oc;
    }

    String getLdapAdminUserFilter();

    public default String getLdapAdminUserFilter(final String name) {
        return String.format(getLdapAdminUserFilter(), name);
    }

    String getLdapAdminUserBaseDn();

    public default String getLdapAdminUserSecurityPrincipal(final String name) {
        final StringBuilder buf = new StringBuilder(100);
        buf.append(String.format(getLdapAdminUserFilter(), name));
        if (StringUtil.isNotBlank(getLdapAdminUserBaseDn())) {
            buf.append(',').append(getLdapAdminUserBaseDn());
        }
        return buf.toString();
    }

    String getLdapAdminRoleObjectClasses();

    public default Attribute getLdapAdminRoleObjectClassAttribute() {
        final Attribute oc = new BasicAttribute("objectClass");
        split(getLdapAdminRoleObjectClasses(), ",").of(stream -> stream.filter(StringUtil::isNotBlank).forEach(s -> oc.add(s.trim())));
        return oc;
    }

    String getLdapAdminRoleFilter();

    public default String getLdapAdminRoleFilter(final String name) {
        return String.format(getLdapAdminRoleFilter(), name);
    }

    String getLdapAdminRoleBaseDn();

    public default String getLdapAdminRoleSecurityPrincipal(final String name) {
        final StringBuilder buf = new StringBuilder(100);
        buf.append(String.format(getLdapAdminRoleFilter(), name));
        if (StringUtil.isNotBlank(getLdapAdminRoleBaseDn())) {
            buf.append(',').append(getLdapAdminRoleBaseDn());
        }
        return buf.toString();
    }

    String getLdapAdminGroupObjectClasses();

    public default Attribute getLdapAdminGroupObjectClassAttribute() {
        final Attribute oc = new BasicAttribute("objectClass");
        split(getLdapAdminGroupObjectClasses(), ",").of(stream -> stream.filter(StringUtil::isNotBlank).forEach(s -> oc.add(s.trim())));
        return oc;
    }

    String getLdapAdminGroupFilter();

    public default String getLdapAdminGroupFilter(final String name) {
        return String.format(getLdapAdminGroupFilter(), name);
    }

    String getLdapAdminGroupBaseDn();

    public default String getLdapAdminGroupSecurityPrincipal(final String name) {
        final StringBuilder buf = new StringBuilder(100);
        buf.append(String.format(getLdapAdminGroupFilter(), name));
        if (StringUtil.isNotBlank(getLdapAdminGroupBaseDn())) {
            buf.append(',').append(getLdapAdminGroupBaseDn());
        }
        return buf.toString();
    }

    String getAuthenticationAdminUsers();

    public default boolean isAdminUser(final String username) {
        return split(getAuthenticationAdminUsers(), ",").get(stream -> stream.anyMatch(s -> s.equals(username)));
    }

    boolean isLdapAdminEnabled();

    public default boolean isLdapAdminEnabled(final String username) {
        if (isAdminUser(username)) {
            return false;
        }
        return isLdapAdminEnabled();
    }

    String getCrawlerWebProtocols();

    public default String[] getCrawlerWebProtocolsAsArray() {
        return split(getCrawlerWebProtocols(), ",").get(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim() + ":").toArray(n -> new String[n]));
    }

    public default boolean isValidCrawlerWebProtocol(final String url) {
        return stream(getCrawlerWebProtocolsAsArray()).get(stream -> stream.anyMatch(s -> url.startsWith(s)));
    }

    String getCrawlerFileProtocols();

    public default String[] getCrawlerFileProtocolsAsArray() {
        return split(getCrawlerFileProtocols(), ",").get(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim() + ":").toArray(n -> new String[n]));
    }

    public default boolean isValidCrawlerFileProtocol(final String url) {
        return stream(getCrawlerFileProtocolsAsArray()).get(stream -> stream.anyMatch(s -> url.startsWith(s)));
    }

    String getRoleSearchDefaultPermissions();

    public default String[] getSearchDefaultPermissionsAsArray() {
        final PermissionHelper permissionHelper = ComponentUtil.getPermissionHelper();
        return split(getRoleSearchDefaultPermissions(), ",")
                .get(stream -> stream.map(p -> permissionHelper.encode(p)).filter(StringUtil::isNotBlank).distinct()
                        .toArray(n -> new String[n]));
    }

    String getRoleSearchDefaultDisplayPermissions();

    public default String[] getSearchDefaultDisplayEncodedPermissions() {
        final PermissionHelper permissionHelper = ComponentUtil.getPermissionHelper();
        return split(getRoleSearchDefaultDisplayPermissions(), ",")
                .get(stream -> stream.map(p -> permissionHelper.encode(p)).filter(StringUtil::isNotBlank).distinct()
                        .toArray(n -> new String[n]));
    }

    public default String getSearchDefaultDisplayPermission() {
        return split(getRoleSearchDefaultDisplayPermissions(), ",").get(
                stream -> stream.filter(StringUtil::isNotBlank).distinct().collect(Collectors.joining("\n")));
    }

    String getQueryGeoFields();

    public default String[] getQueryGeoFieldsAsArray() {
        return split(getQueryGeoFields(), ",").get(
                stream -> stream.map(s -> s.trim()).filter(StringUtil::isNotBlank).toArray(n -> new String[n]));
    }

    String getSuggestSearchLogPermissions();

    public default boolean isValidSearchLogPermissions(final String[] permissions) {
        if (permissions == null) {
            return false;
        }
        @SuppressWarnings("unchecked")
        List<String> validPermissionList = (List<String>) propMap.get(SUGGEST_SEARCH_LOG_PERMISSIONS);
        if (validPermissionList == null) {
            final PermissionHelper permissionHelper = ComponentUtil.getPermissionHelper();
            validPermissionList =
                    split(getSuggestSearchLogPermissions(), ",").get(
                            stream -> stream.map(s -> permissionHelper.encode(s)).filter(StringUtil::isNotBlank)
                                    .collect(Collectors.toList()));
            propMap.put(SUGGEST_SEARCH_LOG_PERMISSIONS, validPermissionList);
        }
        final List<String> list = validPermissionList;
        return stream(permissions).get(stream -> stream.allMatch(v -> list.contains(v)));
    }

    String getRoleSearchUserPrefix();

    String getRoleSearchGuestPermissions();

    public default List<String> getSearchGuestPermissionList() {
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) propMap.get(SEARCH_GUEST_PERMISSION_LIST);
        if (list == null) {
            final PermissionHelper permissionHelper = ComponentUtil.getPermissionHelper();
            list =
                    split(getRoleSearchGuestPermissions(), ",").get(
                            stream -> stream.map(s -> permissionHelper.encode(s)).filter(StringUtil::isNotBlank)
                                    .collect(Collectors.toList()));
            list.add(getRoleSearchUserPrefix() + Constants.GUEST_USER);
            propMap.put(SEARCH_GUEST_PERMISSION_LIST, list);
        }
        return list;
    }

    String getOicDefaultGroups();

    public default String[] getOicDefaultGroupsAsArray() {
        String[] array = (String[]) propMap.get(OIC_DEFAULT_GROUPS);
        if (array == null) {
            if (StringUtil.isBlank(getOicDefaultGroups())) {
                array = StringUtil.EMPTY_STRINGS;
            } else {
                array =
                        split(getOicDefaultGroups(), ",").get(
                                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).toArray(n -> new String[n]));
            }
            propMap.put(OIC_DEFAULT_GROUPS, array);
        }
        return array;
    }

    String getOicDefaultRoles();

    public default String[] getOicDefaultRolesAsArray() {
        String[] array = (String[]) propMap.get(OIC_DEFAULT_ROLES);
        if (array == null) {
            if (StringUtil.isBlank(getOicDefaultRoles())) {
                array = StringUtil.EMPTY_STRINGS;
            } else {
                array =
                        split(getOicDefaultRoles(), ",").get(
                                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).toArray(n -> new String[n]));
            }
            propMap.put(OIC_DEFAULT_ROLES, array);
        }
        return array;
    }

    String getIndexAdminArrayFields();

    public default Set<String> getIndexAdminArrayFieldSet() {
        @SuppressWarnings("unchecked")
        Set<String> fieldSet = (Set<String>) propMap.get(INDEX_ADMIN_ARRAY_FIELD_SET);
        if (fieldSet == null) {
            fieldSet =
                    split(getIndexAdminArrayFields(), ",").get(
                            stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).collect(Collectors.toSet()));
            propMap.put(INDEX_ADMIN_ARRAY_FIELD_SET, fieldSet);
        }
        return fieldSet;
    }

    public default boolean validateIndexArrayFields(final Map<String, Object> source) {
        return invalidIndexArrayFields(source).isEmpty();
    }

    public default List<String> invalidIndexArrayFields(final Map<String, Object> source) {
        // TODO always returns empty list
        return split(getIndexAdminArrayFields(), ",").get(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).filter(s -> isNonEmptyValue(source.get(s)))
                        .filter(s -> false) // TODO
                        .collect(Collectors.toList()));
    }

    String getIndexAdminDateFields();

    public default Set<String> getIndexAdminDateFieldSet() {
        @SuppressWarnings("unchecked")
        Set<String> fieldSet = (Set<String>) propMap.get(INDEX_ADMIN_DATE_FIELD_SET);
        if (fieldSet == null) {
            fieldSet =
                    split(getIndexAdminDateFields(), ",").get(
                            stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).collect(Collectors.toSet()));
            propMap.put(INDEX_ADMIN_DATE_FIELD_SET, fieldSet);
        }
        return fieldSet;
    }

    public default boolean validateIndexDateFields(final Map<String, Object> source) {
        return invalidIndexDateFields(source).isEmpty();
    }

    public default List<String> invalidIndexDateFields(final Map<String, Object> source) {
        return split(getIndexAdminDateFields(), ",").get(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).filter(s -> isNonEmptyValue(source.get(s)))
                        .filter(s -> !validateDateTimeString(source.get(s))).collect(Collectors.toList()));
    }

    public default boolean validateDateTimeString(final Object obj) {
        if (FessFunctions.parseDate(obj.toString()) != null) {
            return true;
        }
        return false;
    }

    String getIndexAdminIntegerFields();

    public default Set<String> getIndexAdminIntegerFieldSet() {
        @SuppressWarnings("unchecked")
        Set<String> fieldSet = (Set<String>) propMap.get(INDEX_ADMIN_INTEGER_FIELD_SET);
        if (fieldSet == null) {
            fieldSet =
                    split(getIndexAdminIntegerFields(), ",").get(
                            stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).collect(Collectors.toSet()));
            propMap.put(INDEX_ADMIN_INTEGER_FIELD_SET, fieldSet);
        }
        return fieldSet;
    }

    public default boolean validateIndexIntegerFields(final Map<String, Object> source) {
        return invalidIndexIntegerFields(source).isEmpty();
    }

    public default List<String> invalidIndexIntegerFields(final Map<String, Object> source) {
        final IntegerTypeValidator integerValidator = new IntegerTypeValidator();
        return split(getIndexAdminIntegerFields(), ",").get(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).filter(s -> isNonEmptyValue(source.get(s)))
                        .filter(s -> !integerValidator.isValid(source.get(s).toString(), null)).collect(Collectors.toList()));
    }

    String getIndexAdminLongFields();

    public default Set<String> getIndexAdminLongFieldSet() {
        @SuppressWarnings("unchecked")
        Set<String> fieldSet = (Set<String>) propMap.get(INDEX_ADMIN_LONG_FIELD_SET);
        if (fieldSet == null) {
            fieldSet =
                    split(getIndexAdminLongFields(), ",").get(
                            stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).collect(Collectors.toSet()));
            propMap.put(INDEX_ADMIN_LONG_FIELD_SET, fieldSet);
        }
        return fieldSet;
    }

    public default boolean validateIndexLongFields(final Map<String, Object> source) {
        return invalidIndexLongFields(source).isEmpty();
    }

    public default List<String> invalidIndexLongFields(final Map<String, Object> source) {
        final LongTypeValidator longValidator = new LongTypeValidator();
        return split(getIndexAdminLongFields(), ",").get(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).filter(s -> isNonEmptyValue(source.get(s)))
                        .filter(s -> !longValidator.isValid(source.get(s).toString(), null)).collect(Collectors.toList()));
    }

    String getIndexAdminFloatFields();

    public default Set<String> getIndexAdminFloatFieldSet() {
        @SuppressWarnings("unchecked")
        Set<String> fieldSet = (Set<String>) propMap.get(INDEX_ADMIN_FLOAT_FIELD_SET);
        if (fieldSet == null) {
            fieldSet =
                    split(getIndexAdminFloatFields(), ",").get(
                            stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).collect(Collectors.toSet()));
            propMap.put(INDEX_ADMIN_FLOAT_FIELD_SET, fieldSet);
        }
        return fieldSet;
    }

    public default boolean validateIndexFloatFields(final Map<String, Object> source) {
        return invalidIndexFloatFields(source).isEmpty();
    }

    public default List<String> invalidIndexFloatFields(final Map<String, Object> source) {
        final FloatTypeValidator floatValidator = new FloatTypeValidator();
        return split(getIndexAdminFloatFields(), ",").get(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).filter(s -> isNonEmptyValue(source.get(s)))
                        .filter(s -> !floatValidator.isValid(source.get(s).toString(), null)).collect(Collectors.toList()));
    }

    String getIndexAdminDoubleFields();

    public default Set<String> getIndexAdminDoubleFieldSet() {
        @SuppressWarnings("unchecked")
        Set<String> fieldSet = (Set<String>) propMap.get(INDEX_ADMIN_DOUBLE_FIELD_SET);
        if (fieldSet == null) {
            fieldSet =
                    split(getIndexAdminDoubleFields(), ",").get(
                            stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).collect(Collectors.toSet()));
            propMap.put(INDEX_ADMIN_DOUBLE_FIELD_SET, fieldSet);
        }
        return fieldSet;
    }

    public default boolean validateIndexDoubleFields(final Map<String, Object> source) {
        return invalidIndexDoubleFields(source).isEmpty();
    }

    public default List<String> invalidIndexDoubleFields(final Map<String, Object> source) {
        final DoubleTypeValidator doubleValidator = new DoubleTypeValidator();
        return split(getIndexAdminDoubleFields(), ",").get(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).filter(s -> isNonEmptyValue(source.get(s)))
                        .filter(s -> !doubleValidator.isValid(source.get(s).toString(), null)).collect(Collectors.toList()));
    }

    public default Map<String, Object> convertToEditableDoc(final Map<String, Object> source) {

        final Set<String> arrayFieldSet = getIndexAdminArrayFieldSet();
        final Set<String> dateFieldSet = getIndexAdminDateFieldSet();
        final Set<String> integerFieldSet = getIndexAdminIntegerFieldSet();
        final Set<String> longFieldSet = getIndexAdminLongFieldSet();
        final Set<String> floatFieldSet = getIndexAdminFloatFieldSet();
        final Set<String> doubleFieldSet = getIndexAdminDoubleFieldSet();

        return source.entrySet().stream().map(e -> {
            final String key = e.getKey();
            Object value = e.getValue();
            if (value == null) {
                value = StringUtil.EMPTY;
            }
            if (value instanceof String || value == null) {
                return new Pair<>(key, value);
            }
            if (arrayFieldSet.contains(key)) {
                if (value instanceof String[]) {
                    value = stream((String[]) value).get(stream -> stream.collect(Collectors.joining("\n")));
                } else if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    final List<String> list = (List<String>) value;
                    value = list.stream().collect(Collectors.joining("\n"));
                }
            } else if (dateFieldSet.contains(key)) {
                value = FessFunctions.formatDate((Date) value);
            } else if (integerFieldSet.contains(key)) {
                value = value.toString();
            } else if (longFieldSet.contains(key)) {
                value = value.toString();
            } else if (floatFieldSet.contains(key)) {
                value = value.toString();
            } else if (doubleFieldSet.contains(key)) {
                value = value.toString();
            }
            return new Pair<>(key, value);
        }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    public default Map<String, Object> convertToStorableDoc(final Map<String, Object> source) {

        final Set<String> arrayFieldSet = getIndexAdminArrayFieldSet();
        final Set<String> dateFieldSet = getIndexAdminDateFieldSet();
        final Set<String> integerFieldSet = getIndexAdminIntegerFieldSet();
        final Set<String> longFieldSet = getIndexAdminLongFieldSet();
        final Set<String> floatFieldSet = getIndexAdminFloatFieldSet();
        final Set<String> doubleFieldSet = getIndexAdminDoubleFieldSet();

        return source
                .entrySet()
                .stream()
                .filter(e -> isNonEmptyValue(e.getValue()))
                .map(e -> {
                    final String key = e.getKey();
                    Object value = e.getValue();
                    if (arrayFieldSet.contains(key)) {
                        value =
                                split(value.toString(), "\n").get(
                                        stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).collect(Collectors.toList()));
                    } else if (dateFieldSet.contains(key)) {
                        // TODO time zone
                        value = FessFunctions.parseDate(value.toString());
                    } else if (integerFieldSet.contains(key)) {
                        value = Integer.parseInt(value.toString());
                    } else if (longFieldSet.contains(key)) {
                        value = Long.parseLong(value.toString());
                    } else if (floatFieldSet.contains(key)) {
                        value = Float.parseFloat(value.toString());
                    } else if (doubleFieldSet.contains(key)) {
                        value = Double.parseDouble(value.toString());
                    }
                    return new Pair<>(key, value);
                }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    String getIndexAdminRequiredFields();

    public default boolean validateIndexRequiredFields(final Map<String, Object> source) {
        return invalidIndexRequiredFields(source).isEmpty();
    }

    public default List<String> invalidIndexRequiredFields(final Map<String, Object> source) {
        final RequiredValidator requiredValidator = new RequiredValidator();
        return split(getIndexAdminRequiredFields(), ",").get(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim())
                        .filter(s -> !requiredValidator.isValid(source.get(s), null)).collect(Collectors.toList()));
    }

    public default boolean isNonEmptyValue(final Object value) {
        final RequiredValidator requiredValidator = new RequiredValidator();
        return requiredValidator.isValid(value, null);
    }

    String getCrawlerDocumentSpaceChars();

    public default int[] getCrawlerDocumentSpaceCharsAsArray() {
        return getCrawlerDocumentCharsAsArray(CRAWLER_DOCUMENT_SPACE_CHARS, getCrawlerDocumentSpaceChars());
    }

    public default int[] getCrawlerDocumentCharsAsArray(final String key, final String spaceStr) {
        int[] spaceChars = (int[]) propMap.get(key);
        if (spaceChars == null) {
            if (spaceStr.startsWith("u")) {
                spaceChars =
                        split(spaceStr, "u").get(
                                stream -> stream.filter(StringUtil::isNotBlank).mapToInt(s -> Integer.parseInt(s, 16)).toArray());
            } else {
                // backward compatibility
                final int length = spaceStr.length();
                spaceChars = new int[length];
                for (int i = 0; i < length; i++) {
                    spaceChars[i] = spaceStr.codePointAt(i);
                }
            }
            propMap.put(key, spaceChars);
        }
        return spaceChars;
    }

    String getCrawlerDocumentFullstopChars();

    public default boolean endsWithFullstop(final String s) {
        if (StringUtil.isBlank(s)) {
            return false;
        }
        for (final int i : getCrawlerDocumentFullstopCharsAsArray()) {
            if (s.endsWith(String.valueOf(i))) {
                return true;
            }
        }
        return false;
    }

    public default int[] getCrawlerDocumentFullstopCharsAsArray() {
        return getCrawlerDocumentCharsAsArray(CRAWLER_DOCUMENT_FULLSTOP_CHARS, getCrawlerDocumentFullstopChars());
    }

    String getQueryAdditionalResponseFields();

    public default String[] getQueryAdditionalResponseFields(final String... fields) {
        final List<String> list = new ArrayList<>(fields.length + 10);
        stream(fields).of(stream -> stream.forEach(list::add));
        split(getQueryAdditionalResponseFields(), ",").of(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).forEach(list::add));
        return list.toArray(new String[list.size()]);
    }

    String getQueryAdditionalScrollResponseFields();

    public default String[] getQueryAdditionalScrollResponseFields(final String... fields) {
        final List<String> list = new ArrayList<>(fields.length + 10);
        stream(fields).of(stream -> stream.forEach(list::add));
        split(getQueryAdditionalScrollResponseFields(), ",").of(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).forEach(list::add));
        return list.toArray(new String[list.size()]);
    }

    String getQueryAdditionalCacheResponseFields();

    public default String[] getQueryAdditionalCacheResponseFields(final String... fields) {
        final List<String> list = new ArrayList<>(fields.length + 10);
        stream(fields).of(stream -> stream.forEach(list::add));
        split(getQueryAdditionalCacheResponseFields(), ",").of(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).forEach(list::add));
        return list.toArray(new String[list.size()]);
    }

    String getQueryAdditionalHighlightedFields();

    public default String[] getQueryAdditionalHighlightedFields(final String... fields) {
        final List<String> list = new ArrayList<>(fields.length + 10);
        stream(fields).of(stream -> stream.forEach(list::add));
        split(getQueryAdditionalHighlightedFields(), ",").of(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).forEach(list::add));
        return list.toArray(new String[list.size()]);
    }

    String getQueryAdditionalSearchFields();

    public default String[] getQueryAdditionalSearchFields(final String... fields) {
        final List<String> list = new ArrayList<>(fields.length + 10);
        stream(fields).of(stream -> stream.forEach(list::add));
        split(getQueryAdditionalSearchFields(), ",").of(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).forEach(list::add));
        return list.toArray(new String[list.size()]);
    }

    String getQueryAdditionalFacetFields();

    public default String[] getQueryAdditionalFacetFields(final String... fields) {
        final List<String> list = new ArrayList<>(fields.length + 10);
        stream(fields).of(stream -> stream.forEach(list::add));
        split(getQueryAdditionalFacetFields(), ",").of(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).forEach(list::add));
        return list.toArray(new String[list.size()]);
    }

    String getQueryAdditionalSortFields();

    public default String[] getQueryAdditionalSortFields(final String... fields) {
        final List<String> list = new ArrayList<>(fields.length + 10);
        stream(fields).of(stream -> stream.forEach(list::add));
        split(getQueryAdditionalSortFields(), ",")
                .of(stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).forEach(list::add));
        return list.toArray(new String[list.size()]);
    }

    String getQueryAdditionalApiResponseFields();

    public default String[] getQueryAdditionalApiResponseFields(final String... fields) {
        final List<String> list = new ArrayList<>(fields.length + 10);
        stream(fields).of(stream -> stream.forEach(list::add));
        split(getQueryAdditionalApiResponseFields(), ",").of(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).forEach(list::add));
        return list.toArray(new String[list.size()]);
    }

    String getQueryAdditionalNotAnalyzedFields();

    public default String[] getQueryAdditionalNotAnalyzedFields(final String... fields) {
        final List<String> list = new ArrayList<>(fields.length + 10);
        stream(fields).of(stream -> stream.forEach(list::add));
        split(getQueryAdditionalNotAnalyzedFields(), ",").of(
                stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).forEach(list::add));
        return list.toArray(new String[list.size()]);
    }

    String getThumbnailGeneratorTargets();

    public default String[] getThumbnailGeneratorTargetsAsArray() {
        return getThumbnailGeneratorTargets().split(",");

    }

    String getApiAdminAccessPermissions();

    public default Set<String> getApiAdminAccessPermissionSet() {
        @SuppressWarnings("unchecked")
        Set<String> fieldSet = (Set<String>) propMap.get(API_ADMIN_ACCESS_PERMISSION_SET);
        if (fieldSet == null) {
            fieldSet =
                    split(getApiAdminAccessPermissions(), ",").get(
                            stream -> stream.filter(StringUtil::isNotBlank).map(s -> s.trim()).collect(Collectors.toSet()));
            propMap.put(API_ADMIN_ACCESS_PERMISSION_SET, fieldSet);
        }
        return fieldSet;
    }

    public default boolean isApiAdminAccessAllowed(final Set<String> accessPermissions) {
        return getApiAdminAccessPermissionSet().stream().anyMatch(s -> accessPermissions.contains(s));
    }

    String getUserCodePattern();

    public default boolean isValidUserCode(final String userCode) {
        if (userCode == null) {
            return false;
        }
        Pattern pattern = (Pattern) propMap.get(USER_CODE_PATTERN);
        if (pattern == null) {
            pattern = Pattern.compile(getUserCodePattern());
            propMap.put(USER_CODE_PATTERN, pattern);
        }
        return pattern.matcher(userCode).matches();
    }

    String getQueryCollapseInnerHitsSorts();

    @SuppressWarnings("rawtypes")
    public default OptionalThing<SortBuilder[]> getQueryCollapseInnerHitsSortBuilders() {
        @SuppressWarnings("unchecked")
        OptionalThing<SortBuilder[]> ot = (OptionalThing<SortBuilder[]>) propMap.get(QUERY_COLLAPSE_INNER_HITS_SORTS);
        if (ot == null) {
            final String sorts = getQueryCollapseInnerHitsSorts();
            if (StringUtil.isBlank(sorts)) {
                ot = OptionalThing.empty();
            } else {
                final SortBuilder[] sortBuilders =
                        split(sorts, ",").get(
                                stream -> stream
                                        .filter(StringUtil::isNotBlank)
                                        .map(s -> {
                                            final String[] values = s.split(":");
                                            if (values.length > 1) {
                                                return SortBuilders.fieldSort(values[0]).order(
                                                        values[0].equalsIgnoreCase("desc") ? SortOrder.DESC : SortOrder.ASC);
                                            } else {
                                                return SortBuilders.fieldSort(values[0]).order(SortOrder.ASC);
                                            }
                                        }).toArray(n -> new SortBuilder[n]));
                ot = OptionalThing.of(sortBuilders);
            }
            propMap.put(QUERY_COLLAPSE_INNER_HITS_SORTS, ot);
        }
        return ot;
    }

    String getVirtualHostHeaders();

    public default String getVirtualHostHeaderValue() {
        final String value = getVirtualHostValue();
        if (StringUtil.isNotBlank(value)) {
            return value;
        }
        return getVirtualHostHeaders();
    }

    @SuppressWarnings("unchecked")
    public default Tuple3<String, String, String>[] getVirtualHosts() {
        Tuple3<String, String, String>[] hosts = (Tuple3<String, String, String>[]) propMap.get(VIRTUAL_HOST_HEADERS);
        if (hosts == null) {
            hosts =
                    split(getVirtualHostHeaderValue(), "\n").get(
                            stream -> stream
                                    .map(s -> {
                                        final String[] v1 = s.split("=");
                                        if (v1.length == 2) {
                                            final String[] v2 = v1[0].split(":", 2);
                                            if (v2.length == 2) {
                                                return new Tuple3<>(v2[0].trim(), v2[1].trim(), v1[1].replaceAll("[^a-zA-Z0-9_]",
                                                        StringUtil.EMPTY).trim());
                                            }
                                        }
                                        return null;
                                    })
                                    .filter(v -> {
                                        if (v == null) {
                                            return false;
                                        }
                                        if ("admin".equalsIgnoreCase(v.getValue3()) || "common".equalsIgnoreCase(v.getValue3())
                                                || "error".equalsIgnoreCase(v.getValue3()) || "login".equalsIgnoreCase(v.getValue3())
                                                || "profile".equalsIgnoreCase(v.getValue3())) {
                                            return false;
                                        }
                                        return true;
                                    }).toArray(n -> new Tuple3[n]));
            propMap.put(VIRTUAL_HOST_HEADERS, hosts);
        }
        return hosts;
    }

    String getCrawlerFailureUrlStatusCodes();

    public default boolean isCrawlerFailureUrlStatusCodes(final int code) {
        int[] codes = (int[]) propMap.get(CRAWLER_FAILURE_URL_STATUS_CODES);
        if (codes == null) {
            codes =
                    split(getCrawlerFailureUrlStatusCodes(), ",").get(
                            stream -> stream.filter(StringUtil::isNotBlank).mapToInt(Integer::parseInt).toArray());
            propMap.put(CRAWLER_FAILURE_URL_STATUS_CODES, codes);
        }
        for (final int v : codes) {
            if (v == code) {
                return true;
            }
        }
        return false;
    }

    Integer getThumbnailHtmlImageMinWidthAsInteger();

    Integer getThumbnailHtmlImageMinHeightAsInteger();

    java.math.BigDecimal getThumbnailHtmlImageMaxAspectRatioAsDecimal();

    public default boolean validateThumbnailSize(final int width, final int height) {
        if (width <= 0 || height <= 0) {
            return false;
        }

        if (width < getThumbnailHtmlImageMinWidthAsInteger().intValue() || height < getThumbnailHtmlImageMinHeightAsInteger().intValue()) {
            return false;
        }

        final float ratio = getThumbnailHtmlImageMaxAspectRatioAsDecimal().floatValue();
        if (((float) width) / ((float) height) > ratio || ((float) height) / ((float) width) > ratio) {
            return false;
        }

        return true;
    }

    String getHttpProxyHost();

    Integer getHttpProxyPortAsInteger();

    String getHttpProxyUsername();

    String getHttpProxyPassword();

    public default Proxy getHttpProxy() {
        Proxy proxy = (Proxy) propMap.get(HTML_PROXY);
        if (proxy == null) {
            if (StringUtil.isNotBlank(getHttpProxyHost()) && getHttpProxyPortAsInteger() != null) {
                final SocketAddress addr = new InetSocketAddress(getHttpProxyHost(), getHttpProxyPortAsInteger());
                proxy = new Proxy(Type.HTTP, addr);
                if (StringUtil.isNotBlank(getHttpProxyUsername())) {
                    Authenticator.setDefault(new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(getHttpProxyUsername(), getHttpProxyPassword().toCharArray());
                        }
                    });
                }
            } else {
                proxy = Proxy.NO_PROXY;
            }
            propMap.put(HTML_PROXY, proxy);
        }
        return proxy;
    }

    String getThumbnailHtmlImageExcludeExtensions();

    public default boolean isThumbnailHtmlImageUrl(final String url) {
        if (StringUtil.isBlank(url)) {
            return false;
        }

        String[] excludeExtensions = (String[]) propMap.get(THUMBNAIL_HTML_IMAGE_EXCLUDE_EXTENSIONS);
        if (excludeExtensions == null) {
            excludeExtensions =
                    split(getThumbnailHtmlImageExcludeExtensions(), ",").get(
                            stream -> stream.map(s -> s.toLowerCase(Locale.ROOT).trim()).filter(StringUtil::isNotBlank)
                                    .toArray(n -> new String[n]));
            propMap.put(THUMBNAIL_HTML_IMAGE_EXCLUDE_EXTENSIONS, excludeExtensions);
        }

        final String u = url.toLowerCase(Locale.ROOT);
        return !stream(excludeExtensions).get(stream -> stream.anyMatch(s -> u.endsWith(s)));
    }

    String getQueryGsaResponseFields();

    public default boolean isGsaResponseFields(final String name) {
        @SuppressWarnings("unchecked")
        Set<String> gsaResponseFieldSet = (Set<String>) propMap.get(QUERY_GSA_RESPONSE_FIELDS);
        if (gsaResponseFieldSet == null) {
            gsaResponseFieldSet =
                    split(getQueryGsaResponseFields(), ",").get(
                            stream -> stream.map(s -> s.toLowerCase(Locale.ROOT).trim()).filter(StringUtil::isNotBlank)
                                    .collect(Collectors.toSet()));
            propMap.put(QUERY_GSA_RESPONSE_FIELDS, gsaResponseFieldSet);
        }
        return gsaResponseFieldSet.contains(name.toLowerCase(Locale.ROOT));
    }

    String getApiSearchAcceptReferers();

    public default boolean isAcceptedSearchReferer(final String referer) {
        Pattern[] patterns = (Pattern[]) propMap.get(API_SEARCH_ACCEPT_REFERERS);
        if (patterns == null) {
            final String refs = getApiSearchAcceptReferers();
            if (StringUtil.isBlank(refs)) {
                patterns = new Pattern[0];
            } else {
                patterns =
                        split(refs, "\n").get(
                                stream -> stream.filter(StringUtil::isNotBlank).map(s -> Pattern.compile(s.trim()))
                                        .toArray(n -> new Pattern[n]));
            }
            propMap.put(API_SEARCH_ACCEPT_REFERERS, patterns);
        }
        if (patterns.length == 0) {
            return true;
        }

        if (referer == null) {
            return false;
        }
        return Arrays.stream(patterns).anyMatch(p -> p.matcher(referer).matches());
    }

    String getQueryHighlightContentDescriptionFields();

    public default String[] getQueryHighlightContentDescriptionFieldsAsArray() {
        return split(getQueryHighlightContentDescriptionFields(), ",").get(
                stream -> stream.filter(StringUtil::isNotBlank).map(String::trim).toArray(n -> new String[n]));
    }

    boolean isLdapIgnoreNetbiosName();

    public default String getCanonicalLdapName(final String name) {
        if (isLdapIgnoreNetbiosName()) {
            final String[] values = name.split("\\\\");
            if (values.length == 0) {
                return null;
            } else if (values.length == 1) {
                return values[0];
            }
            return String.join("\\", Arrays.copyOfRange(values, 1, values.length));
        }
        return name;
    }

    String getLoggingSearchDocsFields();

    public default String[] getLoggingSearchDocsFieldsAsArray() {
        String[] fields = (String[]) propMap.get(LOGGING_SEARCH_DOCS_FIELDS);
        if (fields == null) {
            fields = split(getLoggingSearchDocsFields(), ",").get(stream -> stream.map(String::trim).toArray(n -> new String[n]));
            propMap.put(LOGGING_SEARCH_DOCS_FIELDS, fields);
        }
        return fields;
    }
}
