package edu.sumdu.tss.elephant.helper;

import edu.sumdu.tss.elephant.helper.exception.HttpException;
import edu.sumdu.tss.elephant.helper.utils.ExceptionUtils;
import edu.sumdu.tss.elephant.helper.utils.MessageBundle;
import edu.sumdu.tss.elephant.model.User;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewHelper {

    public static final String[] FLASH_KEY = {Keys.ERROR_KEY, Keys.INFO_KEY};

    public static void userError(final Context ctx, final Integer code,
                                 final String message, final String icon, final String stacktrace) {
        var model = ViewHelper.defaultVariables(ctx);
        model.put("code", code.toString());
        model.put("message", message);
        model.put("icon", icon);
        model.put("stacktrace", stacktrace);
        ctx.status(code);
        ctx.render("/velocity/error.vm", model);
    }

    public static void userError(HttpException exception, final Context ctx) {
        String stacktrace = ExceptionUtils.stacktrace(exception);
        userError(ctx, exception.getCode(), exception.getMessage(), exception.getIcon(), stacktrace);
    }

    public static Map<String, Object> defaultVariables(final Context context) {
        Map<String, Object> model = new HashMap<>();
        model.put("csrf", context.sessionAttribute("csrf"));
        User user = context.sessionAttribute(Keys.SESSION_CURRENT_USER_KEY);
        model.put("currentUser", user);
        String lang = user == null ? context.sessionAttribute(Keys.LANG_KEY) : user.getLanguage();
        model.put("msg", new MessageBundle(lang));
        model.put(Keys.LANG_KEY, lang);
        var crumbs = breadcrumb(context);
        model.put(Keys.BREADCRUMB_KEY, crumbs);

        try {
            String database = context.pathParam("database");
            model.put("database", database);
            crumbs.add(String.format("<a href='/database/%s/'><ion-icon name=\"server-outline\"></ion-icon>%s</a>", database, database));
        } catch (IllegalArgumentException ex) {
            //Most of routes is DB-related. If not - exception will thrown
        }
        //One-time message, clear after usage
        for (var key : FLASH_KEY) {
            String message = context.sessionAttribute(key);
            if (message != null) {
                model.put(key, message);
                context.sessionAttribute(key, null);
            }
        }
        return model;
    }

    public static List<String> breadcrumb(Context context) {
        List<String> breadcrumb = context.sessionAttribute(Keys.BREADCRUMB_KEY);
        if (breadcrumb == null) {
            breadcrumb = new ArrayList<>();
            breadcrumb.add("<a href='/home'><ion-icon name=\"home-outline\"></ion-icon></a>");
            context.sessionAttribute(Keys.BREADCRUMB_KEY, breadcrumb);
        }
        return breadcrumb;
    }

    public static String pager(int totalPage, int currentPage) {
        StringBuilder pager = new StringBuilder(500);
        pager.append("<ul class=\"pages\">");
        for (int i = 1; i < totalPage; i++) {
            pager.append(String.format("<li><a href=\"?offset=\"%i\">%i</li>\n", i, i));
        }
        pager.append("</ul>");
        return pager.toString();
    }

}
