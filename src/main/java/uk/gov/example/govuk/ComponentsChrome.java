package uk.gov.example.govuk;

/**
 * Ports for chrome components (header, footer, breadcrumbs, pagination, cookie banner, …).
 */
final class ComponentsChrome {

  private ComponentsChrome() {}

  private static final String LOGO_CROWN =
      "    <g>\n" +
      "      <circle cx=\"20\" cy=\"17.6\" r=\"3.7\"/>\n" +
      "      <circle cx=\"10.2\" cy=\"23.5\" r=\"3.7\"/>\n" +
      "      <circle cx=\"3.7\" cy=\"33.2\" r=\"3.7\"/>\n" +
      "      <circle cx=\"31.7\" cy=\"30.6\" r=\"3.7\"/>\n" +
      "      <circle cx=\"43.3\" cy=\"17.6\" r=\"3.7\"/>\n" +
      "      <circle cx=\"53.2\" cy=\"23.5\" r=\"3.7\"/>\n" +
      "      <circle cx=\"59.7\" cy=\"33.2\" r=\"3.7\"/>\n" +
      "      <circle cx=\"31.7\" cy=\"30.6\" r=\"3.7\"/>\n" +
      "      <path d=\"M33.1,9.8c.2-.1.3-.3.5-.5l4.6,2.4v-6.8l-4.6,1.5c-.1-.2-.3-.3-.5-.5l1.9-5.9h-6.7l1.9,5.9c-.2.1-.3.3-.5.5l-4.6-1.5v6.8l4.6-2.4c.1.2.3.3.5.5l-2.6,8c-.9,2.8,1.2,5.7,4.1,5.7h0c3,0,5.1-2.9,4.1-5.7l-2.6-8ZM37,37.9s-3.4,3.8-4.1,6.1c2.2,0,4.2-.5,6.4-2.8l-.7,8.5c-2-2.8-4.4-4.1-5.7-3.8.1,3.1.5,6.7,5.8,7.2,3.7.3,6.7-1.5,7-3.8.4-2.6-2-4.3-3.7-1.6-1.4-4.5,2.4-6.1,4.9-3.2-1.9-4.5-1.8-7.7,2.4-10.9,3,4,2.6,7.3-1.2,11.1,2.4-1.3,6.2,0,4,4.6-1.2-2.8-3.7-2.2-4.2.2-.3,1.7.7,3.7,3,4.2,1.9.3,4.7-.9,7-5.9-1.3,0-2.4.7-3.9,1.7l2.4-8c.6,2.3,1.4,3.7,2.2,4.5.6-1.6.5-2.8,0-5.3l5,1.8c-2.6,3.6-5.2,8.7-7.3,17.5-7.4-1.1-15.7-1.7-24.5-1.7h0c-8.8,0-17.1.6-24.5,1.7-2.1-8.9-4.7-13.9-7.3-17.5l5-1.8c-.5,2.5-.6,3.7,0,5.3.8-.8,1.6-2.3,2.2-4.5l2.4,8c-1.5-1-2.6-1.7-3.9-1.7,2.3,5,5.2,6.2,7,5.9,2.3-.4,3.3-2.4,3-4.2-.5-2.4-3-3.1-4.2-.2-2.2-4.6,1.6-6,4-4.6-3.7-3.7-4.2-7.1-1.2-11.1,4.2,3.2,4.3,6.4,2.4,10.9,2.5-2.8,6.3-1.3,4.9,3.2-1.8-2.7-4.1-1-3.7,1.6.3,2.3,3.3,4.1,7,3.8,5.4-.5,5.7-4.2,5.8-7.2-1.3-.2-3.7,1-5.7,3.8l-.7-8.5c2.2,2.3,4.2,2.7,6.4,2.8-.7-2.3-4.1-6.1-4.1-6.1h10.6,0Z\"/>\n" +
      "    </g>";

  private static final String LOGO_LOGOTYPE =
      "    <circle class=\"govuk-logo-dot\" cx=\"226\" cy=\"36\" r=\"7.3\"/>\n" +
      "    <path d=\"M93.94 41.25c.4 1.81 1.2 3.21 2.21 4.62 1 1.4 2.21 2.41 3.61 3.21s3.21 1.2 5.22 1.2 3.61-.4 4.82-1c1.4-.6 2.41-1.4 3.21-2.41.8-1 1.4-2.01 1.61-3.01s.4-2.01.4-3.01v.14h-10.86v-7.02h20.07v24.08h-8.03v-5.56c-.6.8-1.38 1.61-2.19 2.41-.8.8-1.81 1.2-2.81 1.81-1 .4-2.21.8-3.41 1.2s-2.41.4-3.81.4a18.56 18.56 0 0 1-14.65-6.63c-1.6-2.01-3.01-4.41-3.81-7.02s-1.4-5.62-1.4-8.83.4-6.02 1.4-8.83a20.45 20.45 0 0 1 19.46-13.65c3.21 0 4.01.2 5.82.8 1.81.4 3.61 1.2 5.02 2.01 1.61.8 2.81 2.01 4.01 3.21s2.21 2.61 2.81 4.21l-7.63 4.41c-.4-1-1-1.81-1.61-2.61-.6-.8-1.4-1.4-2.21-2.01-.8-.6-1.81-1-2.81-1.4-1-.4-2.21-.4-3.61-.4-2.01 0-3.81.4-5.22 1.2-1.4.8-2.61 1.81-3.61 3.21s-1.61 2.81-2.21 4.62c-.4 1.81-.6 3.71-.6 5.42s.8 5.22.8 5.22Zm57.8-27.9c3.21 0 6.22.6 8.63 1.81 2.41 1.2 4.82 2.81 6.62 4.82S170.2 24.39 171 27s1.4 5.62 1.4 8.83-.4 6.02-1.4 8.83-2.41 5.02-4.01 7.02-4.01 3.61-6.62 4.82-5.42 1.81-8.63 1.81-6.22-.6-8.63-1.81-4.82-2.81-6.42-4.82-3.21-4.41-4.01-7.02-1.4-5.62-1.4-8.83.4-6.02 1.4-8.83 2.41-5.02 4.01-7.02 4.01-3.61 6.42-4.82 5.42-1.81 8.63-1.81Zm0 36.73c1.81 0 3.61-.4 5.02-1s2.61-1.81 3.61-3.01 1.81-2.81 2.21-4.41c.4-1.81.8-3.61.8-5.62 0-2.21-.2-4.21-.8-6.02s-1.2-3.21-2.21-4.62c-1-1.2-2.21-2.21-3.61-3.01s-3.21-1-5.02-1-3.61.4-5.02 1c-1.4.8-2.61 1.81-3.61 3.01s-1.81 2.81-2.21 4.62c-.4 1.81-.8 3.61-.8 5.62 0 2.41.2 4.21.8 6.02.4 1.81 1.2 3.21 2.21 4.41s2.21 2.21 3.61 3.01c1.4.8 3.21 1 5.02 1Zm36.32 7.96-12.24-44.15h9.83l8.43 32.77h.4l8.23-32.77h9.83L200.3 58.04h-12.24Zm74.14-7.96c2.18 0 3.51-.6 3.51-.6 1.2-.6 2.01-1 2.81-1.81s1.4-1.81 1.81-2.81a13 13 0 0 0 .8-4.01V13.9h8.63v28.15c0 2.41-.4 4.62-1.4 6.62-.8 2.01-2.21 3.61-3.61 5.02s-3.41 2.41-5.62 3.21-4.62 1.2-7.02 1.2-5.02-.4-7.02-1.2c-2.21-.8-4.01-1.81-5.62-3.21s-2.81-3.01-3.61-5.02-1.4-4.21-1.4-6.62V13.9h8.63v26.95c0 1.61.2 3.01.8 4.01.4 1.2 1.2 2.21 2.01 2.81.8.8 1.81 1.4 2.81 1.81 0 0 1.34.6 3.51.6Zm34.22-36.18v18.92l15.65-18.92h10.82l-15.03 17.32 16.03 26.83h-10.21l-11.44-20.21-5.62 6.22v13.99h-8.83V13.9\"/>";

  private static final String FOOTER_LICENCE_LOGO =
      "<svg\n" +
      "            aria-hidden=\"true\"\n" +
      "            focusable=\"false\"\n" +
      "            class=\"govuk-footer__licence-logo\"\n" +
      "            xmlns=\"http://www.w3.org/2000/svg\"\n" +
      "            viewBox=\"0 0 483.2 195.7\"\n" +
      "            height=\"17\"\n" +
      "            width=\"41\"\n" +
      "          >\n" +
      "            <path\n" +
      "              fill=\"currentColor\"\n" +
      "              d=\"M421.5 142.8V.1l-50.7 32.3v161.1h112.4v-50.7zm-122.3-9.6A47.12 47.12 0 0 1 221 97.8c0-26 21.1-47.1 47.1-47.1 16.7 0 31.4 8.7 39.7 21.8l42.7-27.2A97.63 97.63 0 0 0 268.1 0c-36.5 0-68.3 20.1-85.1 49.7A98 98 0 0 0 97.8 0C43.9 0 0 43.9 0 97.8s43.9 97.8 97.8 97.8c36.5 0 68.3-20.1 85.1-49.7a97.76 97.76 0 0 0 149.6 25.4l19.4 22.2h3v-87.8h-80l24.3 27.5zM97.8 145c-26 0-47.1-21.1-47.1-47.1s21.1-47.1 47.1-47.1 47.2 21 47.2 47S123.8 145 97.8 145\"\n" +
      "            />\n" +
      "          </svg>";

  private static final String FOOTER_COPYRIGHT_HREF =
      "https://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/uk-government-licensing-framework/crown-copyright/";

  private static final String PAGINATION_ARROW_PREVIOUS =
      "  <svg class=\"govuk-pagination__icon govuk-pagination__icon--prev\" xmlns=\"http://www.w3.org/2000/svg\" height=\"13\" width=\"15\" aria-hidden=\"true\" focusable=\"false\" viewBox=\"0 0 15 13\">\n" +
      "    <path d=\"m6.5938-0.0078125-6.7266 6.7266 6.7441 6.4062 1.377-1.449-4.1856-3.9768h12.896v-2h-12.984l4.2931-4.293-1.414-1.414z\"></path>\n" +
      "  </svg>";

  private static final String PAGINATION_ARROW_NEXT =
      "  <svg class=\"govuk-pagination__icon govuk-pagination__icon--next\" xmlns=\"http://www.w3.org/2000/svg\" height=\"13\" width=\"15\" aria-hidden=\"true\" focusable=\"false\" viewBox=\"0 0 15 13\">\n" +
      "    <path d=\"m8.107-0.0078125-1.4136 1.414 4.2926 4.293h-12.986v2h12.896l-4.1855 3.9766 1.377 1.4492 6.7441-6.4062-6.7246-6.7266z\"></path>\n" +
      "  </svg>";

  static String renderLogo(Params p) {
    boolean useLogotype = Nunjucks.truthy(Nunjucks.def(p.get("useLogotype"), true));
    String width = "32";
    String viewBox = "64";
    if (useLogotype) {
      width = "162";
      viewBox = "324";
    }
    String role = "presentation";
    Object ariaLabel = p.get("ariaLabelText");
    if (Nunjucks.truthy(ariaLabel)) {
      role = "img";
    }

    StringBuilder out = new StringBuilder();
    out.append("\n  <svg\n    focusable=\"false\"\n    role=\"")
        .append(role)
        .append("\"\n")
        .append("    xmlns=\"http://www.w3.org/2000/svg\"\n")
        .append("    viewBox=\"0 0 ")
        .append(viewBox)
        .append(" 60\"\n    height=\"30\"\n    width=\"")
        .append(width)
        .append("\"\n")
        .append("    fill=\"currentcolor\"")
        .append(Attributes.attributeIf("class", p.get("classes")))
        .append(Attributes.attributeIf("aria-label", ariaLabel))
        .append(Attributes.attributes(p.get("attributes")))
        .append("\n  >");
    if (Nunjucks.truthy(ariaLabel)) {
      out.append("<title>").append(Nunjucks.out(ariaLabel)).append("</title>");
    }
    out.append("    ").append(Nunjucks.indent(Nunjucks.trim(LOGO_CROWN), 2, false)).append('\n');
    if (useLogotype) {
      out.append("      ")
          .append(Nunjucks.indent(Nunjucks.trim(LOGO_LOGOTYPE), 2, false))
          .append('\n');
    }
    out.append("  </svg>\n");
    return out.toString();
  }

  static String renderGenericHeader(Params p) {
    String namespace = Nunjucks.out(Nunjucks.def(p.get("_namespace"), "govuk-generic"));
    return "<div class=\""
        + namespace
        + "-header"
        + Attributes.classesIf(p.get("classes"))
        + '"'
        + Attributes.attributes(p.get("attributes"))
        + ">\n"
        + "  <div class=\""
        + namespace
        + "-header__container "
        + Nunjucks.out(Nunjucks.defTruthy(p.get("containerClasses"), "govuk-width-container"))
        + "\">\n"
        + "    <div class=\""
        + namespace
        + "-header__logo\">\n"
        + "      <a href=\""
        + Nunjucks.out(Nunjucks.defTruthy(p.get("url"), "/"))
        + "\" class=\""
        + namespace
        + "-header__homepage-link\">\n"
        + "        "
        + Nunjucks.content(p, "logoHtml", "logoText")
        + "\n"
        + "      </a>\n    </div>\n  </div>\n</div>";
  }

  static String renderHeader(Params p) {
    String logo =
        renderLogo(
            Params.of(
                "classes", "govuk-header__logotype",
                "ariaLabelText", "GOV.UK"));
    String logoContent = "  " + Nunjucks.trim(logo) + "\n";
    Object productName = p.get("productName");
    if (Nunjucks.truthy(productName)) {
      logoContent +=
          "<span class=\"govuk-header__product-name\">" + Nunjucks.out(productName) + "</span>";
    }

    return renderGenericHeader(
        Params.of(
            "_namespace", "govuk",
            "logoHtml", new TrustedHtml(Nunjucks.indent(logoContent, 8, false)),
            "url", Nunjucks.defTruthy(p.get("homepageUrl"), "//gov.uk"),
            "containerClasses", p.get("containerClasses"),
            "classes", p.get("classes"),
            "attributes", p.get("attributes")));
  }

  static String renderFooter(Params p) {
    StringBuilder out = new StringBuilder();
    out.append("<div class=\"govuk-footer")
        .append(Attributes.classesIf(p.get("classes")))
        .append('"')
        .append(Attributes.attributes(p.get("attributes")))
        .append(">\n");
    out.append("  <div class=\"govuk-width-container")
        .append(Attributes.classesIf(p.get("containerClasses")))
        .append("\">");
    out.append(renderLogo(Params.of("classes", "govuk-footer__crown", "useLogotype", false)));
    out.append('\n');

    var navigation = Nunjucks.items(p.get("navigation"));
    if (!navigation.isEmpty()) {
      out.append("      <div class=\"govuk-footer__navigation\">\n");
      for (Object nav : navigation) {
        out.append("          <div class=\"govuk-footer__section govuk-grid-column-")
            .append(Nunjucks.out(Nunjucks.defTruthy(Nunjucks.get(nav, "width"), "full")))
            .append("\">\n");
        out.append("            <h2 class=\"govuk-footer__heading govuk-heading-m\">")
            .append(Nunjucks.out(Nunjucks.get(nav, "title")))
            .append("</h2>\n");
        var links = Nunjucks.items(Nunjucks.get(nav, "items"));
        if (!links.isEmpty()) {
          String listClasses = "";
          Object columns = Nunjucks.get(nav, "columns");
          if (Nunjucks.truthy(columns)) {
            listClasses = " govuk-footer__list--columns-" + Nunjucks.escape(Nunjucks.str(columns));
          }
          out.append("              <ul class=\"govuk-footer__list")
              .append(listClasses)
              .append("\">\n");
          for (Object link : links) {
            if (!Nunjucks.truthy(Nunjucks.get(link, "href"))
                || !Nunjucks.truthy(Nunjucks.get(link, "text"))) {
              continue;
            }
            out.append("                    <li class=\"govuk-footer__list-item\">\n");
            out.append("                      <a class=\"govuk-footer__link\" href=\"")
                .append(Nunjucks.out(Nunjucks.get(link, "href")))
                .append('"')
                .append(Attributes.attributes(Nunjucks.get(link, "attributes")))
                .append(">\n");
            out.append("                        ")
                .append(Nunjucks.out(Nunjucks.get(link, "text")))
                .append('\n');
            out.append("                      </a>\n                    </li>\n");
          }
          out.append("              </ul>\n");
        }
        out.append("          </div>\n");
      }
      out.append("      </div>\n");
      out.append("      <hr class=\"govuk-footer__section-break\">\n");
    }

    out.append("    <div class=\"govuk-footer__meta\">\n");
    out.append("      <div class=\"govuk-footer__meta-item govuk-footer__meta-item--grow\">\n");

    Object meta = p.get("meta");
    if (Nunjucks.truthy(meta)) {
      out.append("        <h2 class=\"govuk-visually-hidden\">")
          .append(
              Nunjucks.out(
                  Nunjucks.defTruthy(Nunjucks.get(meta, "visuallyHiddenTitle"), "Support links")))
          .append("</h2>\n");
      var links = Nunjucks.items(Nunjucks.get(meta, "items"));
      if (!links.isEmpty()) {
        out.append("        <ul class=\"govuk-footer__inline-list\">\n");
        for (Object link : links) {
          out.append("          <li class=\"govuk-footer__inline-list-item\">\n");
          out.append("            <a class=\"govuk-footer__link\" href=\"")
              .append(Nunjucks.out(Nunjucks.get(link, "href")))
              .append('"')
              .append(Attributes.attributes(Nunjucks.get(link, "attributes")))
              .append(">\n");
          out.append("              ")
              .append(Nunjucks.out(Nunjucks.get(link, "text")))
              .append('\n');
          out.append("            </a>\n          </li>\n");
        }
        out.append("        </ul>\n");
      }
      if (Nunjucks.truthy(Nunjucks.get(meta, "text"))
          || Nunjucks.truthy(Nunjucks.get(meta, "html"))) {
        out.append("        <div class=\"govuk-footer__meta-custom\">\n");
        out.append("          ")
            .append(Nunjucks.contentIndent(meta, "html", "text", 10))
            .append('\n');
        out.append("        </div>\n");
      }
    }

    Object licence = p.get("contentLicence");
    if (licence != null) {
      out.append("          ").append(FOOTER_LICENCE_LOGO).append('\n');
      out.append("          <span class=\"govuk-footer__licence-description\">\n");
      if (Nunjucks.truthy(Nunjucks.get(licence, "html"))
          || Nunjucks.truthy(Nunjucks.get(licence, "text"))) {
        out.append("            ")
            .append(Nunjucks.contentIndent(licence, "html", "text", 12))
            .append('\n');
      } else {
        out.append(
            "            All content is available under the\n"
                + "            <a\n"
                + "              class=\"govuk-footer__link\"\n"
                + "              href=\"https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/\"\n"
                + "              rel=\"license\"\n"
                + "            >Open Government Licence v3.0</a>, except where otherwise stated\n");
      }
      out.append("          </span>\n");
    }

    out.append("      </div>\n");
    out.append("      <div class=\"govuk-footer__meta-item\">\n");
    out.append("        <a\n")
        .append("          class=\"govuk-footer__link govuk-footer__copyright-logo\"\n")
        .append("          href=\"")
        .append(FOOTER_COPYRIGHT_HREF)
        .append("\"\n        >\n");
    Object copyright = p.get("copyright");
    if (Nunjucks.truthy(Nunjucks.get(copyright, "html"))
        || Nunjucks.truthy(Nunjucks.get(copyright, "text"))) {
      out.append("          ")
          .append(Nunjucks.contentIndent(copyright, "html", "text", 10))
          .append('\n');
    } else {
      out.append("          \u00a9 Crown copyright\n");
    }
    out.append("        </a>\n      </div>\n");
    out.append("    </div>\n  </div>\n</div>");
    return out.toString();
  }

  static String renderBreadcrumbs(Params p) {
    String classNames = "govuk-breadcrumbs";
    Object classes = p.get("classes");
    if (Nunjucks.truthy(classes)) {
      classNames += " " + Nunjucks.str(classes);
    }
    if (Nunjucks.truthy(p.get("collapseOnMobile"))) {
      classNames += " govuk-breadcrumbs--collapse-on-mobile";
    }

    StringBuilder out = new StringBuilder();
    out.append("<nav class=\"")
        .append(Nunjucks.escape(classNames))
        .append('"')
        .append(Attributes.attributes(p.get("attributes")))
        .append(" aria-label=\"")
        .append(Nunjucks.out(Nunjucks.def(p.get("labelText"), "Breadcrumb")))
        .append("\">\n");
    out.append("  <ol class=\"govuk-breadcrumbs__list\">\n");
    for (Object item : Nunjucks.items(p.get("items"))) {
      Object href = Nunjucks.get(item, "href");
      if (Nunjucks.truthy(href)) {
        out.append("    <li class=\"govuk-breadcrumbs__list-item\">\n");
        out.append("      <a class=\"govuk-breadcrumbs__link\" href=\"")
            .append(Nunjucks.out(href))
            .append('"')
            .append(Attributes.attributes(Nunjucks.get(item, "attributes")))
            .append('>')
            .append(Nunjucks.content(item, "html", "text"))
            .append("</a>\n");
        out.append("    </li>\n");
      } else {
        out.append("    <li class=\"govuk-breadcrumbs__list-item\" aria-current=\"page\">")
            .append(Nunjucks.content(item, "html", "text"))
            .append("</li>\n");
      }
    }
    out.append("  </ol>\n</nav>");
    return out.toString();
  }

  static String renderLanguageNavigation(Params p) {
    StringBuilder out = new StringBuilder();
    out.append("<nav class=\"govuk-language-navigation")
        .append(Attributes.classesIf(p.get("classes")))
        .append('"')
        .append(Attributes.attributes(p.get("attributes")))
        .append(" aria-label=\"")
        .append(Nunjucks.out(Nunjucks.def(p.get("ariaLabel"), "Language")))
        .append("\">\n");
    out.append("  <ul class=\"govuk-language-navigation__list\">\n");

    for (Object item : Nunjucks.items(p.get("items"))) {
      Object href = Nunjucks.get(item, "href");
      out.append("    <li class=\"govuk-language-navigation__list-item\">\n");
      if (Nunjucks.truthy(Nunjucks.get(item, "current")) || !Nunjucks.truthy(href)) {
        out.append("      <span class=\"govuk-language-navigation__text")
            .append(Attributes.classesIf(Nunjucks.get(item, "classes")))
            .append("\"\n        aria-current=\"true\"")
            .append(Attributes.attributeIf("lang", Nunjucks.get(item, "lang")))
            .append(Attributes.attributeIf("dir", Nunjucks.get(item, "dir")))
            .append(Attributes.attributes(Nunjucks.get(item, "attributes")))
            .append('>')
            .append(Nunjucks.content(item, "html", "text"))
            .append("</span>\n");
      } else {
        Object hrefLang = Nunjucks.get(item, "hrefLang");
        if (!Nunjucks.truthy(hrefLang)) {
          hrefLang = Nunjucks.get(item, "lang");
        }
        out.append("      <a class=\"govuk-language-navigation__link")
            .append(Attributes.classesIf(Nunjucks.get(item, "classes")))
            .append("\" href=\"")
            .append(Nunjucks.out(href))
            .append("\" rel=\"alternate\"")
            .append(Attributes.attributeIf("lang", Nunjucks.get(item, "lang")))
            .append(Attributes.attributeIf("hreflang", hrefLang))
            .append(Attributes.attributeIf("dir", Nunjucks.get(item, "dir")))
            .append(Attributes.attributes(Nunjucks.get(item, "attributes")))
            .append('>')
            .append(Nunjucks.content(item, "html", "text"));
        Object description = Nunjucks.get(item, "languageDescriptionText");
        if (Nunjucks.truthy(description)) {
          out.append("<span class=\"govuk-visually-hidden\"> ")
              .append(Nunjucks.out(description))
              .append("</span>");
        }
        out.append("      </a>\n");
      }
      out.append("    </li>\n");
    }

    out.append("  </ul>\n</nav>");
    return out.toString();
  }

  static String renderServiceNavigation(Params p) {
    Object slots = p.get("slots");
    Object menuButtonText = Nunjucks.defTruthy(p.get("menuButtonText"), "Menu");
    String navigationId = Nunjucks.out(Nunjucks.defTruthy(p.get("navigationId"), "navigation"));

    Object endSlot = Nunjucks.get(slots, "end");
    Object endSlotHtml = Nunjucks.get(endSlot, "html");
    if (endSlot instanceof String text) {
      endSlotHtml = text;
    }
    boolean endSlotInline =
        endSlot instanceof Params endSlotObject
            && Nunjucks.looseEq(endSlotObject.get("align"), "inline");

    String commonAttributes =
        "class=\"govuk-service-navigation"
            + Attributes.classesIf(p.get("classes"))
            + "\"\n"
            + "data-module=\"govuk-service-navigation\""
            + Attributes.attributes(p.get("attributes"))
            + "\n";

    StringBuilder inner = new StringBuilder();
    inner
        .append("  <div class=\"govuk-width-container")
        .append(Attributes.flagIf(" govuk-service-navigation__inlining-container", endSlotInline))
        .append("\">\n\n    ");
    Object start = Nunjucks.get(slots, "start");
    if (Nunjucks.truthy(start)) {
      inner.append(Nunjucks.str(start));
    }
    inner.append("<div class=\"govuk-service-navigation__container\">\n      \n");

    Object serviceName = p.get("serviceName");
    if (Nunjucks.truthy(serviceName)) {
      inner.append("        <span class=\"govuk-service-navigation__service-name\">\n");
      Object serviceUrl = p.get("serviceUrl");
      if (Nunjucks.truthy(serviceUrl)) {
        inner
            .append("            <a href=\"")
            .append(Nunjucks.out(serviceUrl))
            .append("\" class=\"govuk-service-navigation__link\">\n");
        inner
            .append("              ")
            .append(Nunjucks.out(serviceName))
            .append("\n            </a>\n");
      } else {
        inner
            .append("            <span class=\"govuk-service-navigation__text\">")
            .append(Nunjucks.out(serviceName))
            .append("</span>\n");
      }
      inner.append("        </span>\n");
    }
    inner.append("\n      \n");

    java.util.List<Object> navigationItems = new java.util.ArrayList<>();
    for (Object item : Nunjucks.items(p.get("navigation"))) {
      if (Nunjucks.truthy(item)) {
        navigationItems.add(item);
      }
    }
    boolean collapse =
        Nunjucks.truthy(
            Nunjucks.def(p.get("collapseNavigationOnMobile"), navigationItems.size() > 1));

    Object navigationStart = Nunjucks.get(slots, "navigationStart");
    Object navigationEnd = Nunjucks.get(slots, "navigationEnd");
    if (!navigationItems.isEmpty()
        || Nunjucks.truthy(navigationStart)
        || Nunjucks.truthy(navigationEnd)) {
      inner
          .append("        <nav aria-label=\"")
          .append(Nunjucks.out(Nunjucks.defTruthy(p.get("navigationLabel"), menuButtonText)))
          .append("\" class=\"govuk-service-navigation__wrapper")
          .append(Attributes.classesIf(p.get("navigationClasses")))
          .append("\">\n");
      if (collapse) {
        Object menuButtonLabel = p.get("menuButtonLabel");
        String ariaLabel = "";
        if (Nunjucks.truthy(menuButtonLabel)
            && !Nunjucks.looseEq(menuButtonLabel, menuButtonText)) {
          ariaLabel = " aria-label=\"" + Nunjucks.out(menuButtonLabel) + '"';
        }
        inner
            .append(
                "          <button type=\"button\" class=\"govuk-service-navigation__toggle govuk-js-service-navigation-toggle\" aria-controls=\"")
            .append(navigationId)
            .append('"')
            .append(ariaLabel)
            .append(" hidden aria-hidden=\"true\">\n");
        inner
            .append("            ")
            .append(Nunjucks.out(menuButtonText))
            .append("\n          </button>\n");
      }
      inner
          .append("\n          <ul class=\"govuk-service-navigation__list\" id=\"")
          .append(navigationId)
          .append("\" >\n\n            ");
      if (Nunjucks.truthy(navigationStart)) {
        inner.append(Nunjucks.str(navigationStart));
      }
      inner.append('\n');

      for (Object item : navigationItems) {
        boolean active =
            Nunjucks.truthy(Nunjucks.get(item, "active"))
                || Nunjucks.truthy(Nunjucks.get(item, "current"));
        String linkInner;
        if (active) {
          linkInner =
              "\n                                    \n                  <strong class=\"govuk-service-navigation__active-fallback\">"
                  + Nunjucks.content(item, "html", "text")
                  + "</strong>\n";
        } else {
          linkInner =
              "\n                                    \n" + Nunjucks.content(item, "html", "text");
        }

        String ariaCurrent = "";
        if (active) {
          String value = "true";
          if (Nunjucks.truthy(Nunjucks.get(item, "current"))) {
            value = "page";
          }
          ariaCurrent = " aria-current=\"" + value + '"';
        }

        inner.append("              \n");
        inner
            .append("              <li class=\"govuk-service-navigation__item")
            .append(Attributes.flagIf(" govuk-service-navigation__item--active", active))
            .append("\">\n");
        Object href = Nunjucks.get(item, "href");
        if (Nunjucks.truthy(href)) {
          inner
              .append("                  <a class=\"govuk-service-navigation__link\" href=\"")
              .append(Nunjucks.out(href))
              .append('"')
              .append(ariaCurrent)
              .append(Attributes.attributes(Nunjucks.get(item, "attributes")))
              .append('>')
              .append(linkInner)
              .append("\n                  </a>\n");
        } else if (Nunjucks.truthy(Nunjucks.get(item, "html"))
            || Nunjucks.truthy(Nunjucks.get(item, "text"))) {
          inner
              .append("                  <span class=\"govuk-service-navigation__text\"")
              .append(ariaCurrent)
              .append('>')
              .append(linkInner)
              .append("\n                  </span>\n");
        }
        inner.append("              </li>\n\n");
      }

      inner.append("            ");
      if (Nunjucks.truthy(navigationEnd)) {
        inner.append(Nunjucks.str(navigationEnd));
      }
      inner.append("</ul>\n        </nav>\n");
    }

    inner.append("    </div>\n\n    ");
    if (Nunjucks.truthy(endSlotHtml)) {
      inner.append(Nunjucks.str(endSlotHtml));
    }
    inner.append("</div>\n");

    if (Nunjucks.truthy(p.get("serviceName"))
        || Nunjucks.truthy(Nunjucks.get(slots, "start"))
        || Nunjucks.truthy(endSlotHtml)) {
      return "  <section aria-label=\""
          + Nunjucks.out(Nunjucks.def(p.get("ariaLabel"), "Service information"))
          + "\" "
          + commonAttributes
          + ">\n    "
          + inner
          + "\n  </section>\n";
    }
    return "  <div " + commonAttributes + ">\n    " + inner + "\n  </div>\n";
  }

  static String renderPagination(Params p) {
    Object previous = p.get("previous");
    Object next = p.get("next");
    boolean blockLevel =
        !Nunjucks.truthy(p.get("items")) && (Nunjucks.truthy(next) || Nunjucks.truthy(previous));

    StringBuilder out = new StringBuilder();
    out.append("<nav class=\"govuk-pagination")
        .append(Attributes.flagIf(" govuk-pagination--block", blockLevel))
        .append(Attributes.classesIf(p.get("classes")))
        .append("\" aria-label=\"")
        .append(Nunjucks.out(Nunjucks.defTruthy(p.get("landmarkLabel"), "Pagination")))
        .append('"')
        .append(Attributes.attributes(p.get("attributes")))
        .append(">\n");

    if (Nunjucks.truthy(previous) && Nunjucks.truthy(Nunjucks.get(previous, "href"))) {
      out.append(
          paginationArrowLink(
              previous, "prev", blockLevel, paginationLinkLabel(previous, "Previous")));
    }

    Object entries = p.get("items");
    if (Nunjucks.truthy(entries)) {
      out.append("  <ul class=\"govuk-pagination__list\">\n");
      for (Object item : Nunjucks.items(entries)) {
        if (item == null || Nunjucks.length(item) == 0) {
          continue;
        }
        out.append("      ")
            .append(Nunjucks.indent(paginationPageItem(item), 2, false))
            .append('\n');
      }
      out.append("  </ul>\n");
    }

    if (Nunjucks.truthy(next) && Nunjucks.truthy(Nunjucks.get(next, "href"))) {
      out.append(
          paginationArrowLink(next, "next", blockLevel, paginationLinkLabel(next, "Next")));
    }

    out.append("</nav>");
    return out.toString();
  }

  private static String paginationLinkLabel(Object link, String fallback) {
    Object html = Nunjucks.get(link, "html");
    Object text = Nunjucks.get(link, "text");
    if (Nunjucks.truthy(html)) {
      return Nunjucks.trim(Nunjucks.indent(Nunjucks.trim(Nunjucks.str(html)), 8, false));
    }
    if (Nunjucks.truthy(text)) {
      return Nunjucks.out(text);
    }
    return fallback + "<span class=\"govuk-visually-hidden\"> page</span>";
  }

  private static String paginationArrowLink(
      Object link, String kind, boolean blockLevel, String label) {
    String arrow = "next".equals(kind) ? PAGINATION_ARROW_NEXT : PAGINATION_ARROW_PREVIOUS;

    StringBuilder out = new StringBuilder();
    out.append("  <div class=\"govuk-pagination__").append(kind).append("\">\n");
    out.append("    <a class=\"govuk-link govuk-pagination__link\" href=\"")
        .append(Nunjucks.out(Nunjucks.get(link, "href")))
        .append("\" rel=\"")
        .append(kind)
        .append('"')
        .append(Attributes.attributes(Nunjucks.get(link, "attributes")))
        .append(">\n");
    if (blockLevel || "prev".equals(kind)) {
      out.append(Nunjucks.indent(arrow, 4, true)).append('\n');
    }
    Object labelText = Nunjucks.get(link, "labelText");
    out.append("      <span class=\"govuk-pagination__link-title")
        .append(
            Attributes.flagIf(
                " govuk-pagination__link-title--decorated",
                blockLevel && !Nunjucks.truthy(labelText)))
        .append("\">\n        ")
        .append(label)
        .append("\n      </span>\n");
    if (Nunjucks.truthy(labelText) && blockLevel) {
      out.append("      <span class=\"govuk-visually-hidden\">:</span>\n");
      out.append("      <span class=\"govuk-pagination__link-label\">")
          .append(Nunjucks.out(labelText))
          .append("</span>\n");
    }
    if (!blockLevel && "next".equals(kind)) {
      out.append(Nunjucks.indent(arrow, 4, true)).append('\n');
    }
    out.append("    </a>\n  </div>\n");
    return out.toString();
  }

  private static String paginationPageItem(Object item) {
    StringBuilder out = new StringBuilder();
    out.append("<li class=\"govuk-pagination__item")
        .append(Attributes.flagIf(" govuk-pagination__item--current", Nunjucks.get(item, "current")))
        .append(
            Attributes.flagIf(
                " govuk-pagination__item--ellipsis", Nunjucks.get(item, "ellipsis")))
        .append("\">\n");
    if (Nunjucks.truthy(Nunjucks.get(item, "ellipsis"))) {
      out.append("    &ctdot;\n");
    } else {
      out.append("    <a class=\"govuk-link govuk-pagination__link\" href=\"")
          .append(Nunjucks.out(Nunjucks.get(item, "href")))
          .append("\" aria-label=\"")
          .append(
              Nunjucks.out(
                  Nunjucks.def(
                      Nunjucks.get(item, "visuallyHiddenText"),
                      "Page " + Nunjucks.str(Nunjucks.get(item, "number")))))
          .append('"')
          .append(Attributes.flagIf(" aria-current=\"page\"", Nunjucks.get(item, "current")))
          .append(Attributes.attributes(Nunjucks.get(item, "attributes")))
          .append(">\n");
      out.append("      ")
          .append(Nunjucks.out(Nunjucks.get(item, "number")))
          .append("\n    </a>\n");
    }
    out.append("  </li>");
    return out.toString();
  }

  static String renderCookieBanner(Params p) {
    StringBuilder out = new StringBuilder();
    out.append("<div class=\"govuk-cookie-banner")
        .append(Attributes.classesIf(p.get("classes")))
        .append("\" data-nosnippet role=\"region\" aria-label=\"")
        .append(Nunjucks.out(Nunjucks.defTruthy(p.get("ariaLabel"), "Cookie banner")))
        .append('"')
        .append(Attributes.flagIf(" hidden", p.get("hidden")))
        .append(Attributes.attributes(p.get("attributes")))
        .append(">\n");

    for (Object message : Nunjucks.items(p.get("messages"))) {
      out.append("  <div class=\"govuk-cookie-banner__message")
          .append(Attributes.classesIf(Nunjucks.get(message, "classes")))
          .append(" govuk-width-container\"")
          .append(Attributes.attributeIf("role", Nunjucks.get(message, "role")))
          .append(Attributes.attributes(Nunjucks.get(message, "attributes")))
          .append(Attributes.flagIf(" hidden", Nunjucks.get(message, "hidden")))
          .append(">\n\n");
      out.append("    <div class=\"govuk-grid-row\">\n");
      out.append("      <div class=\"govuk-grid-column-two-thirds\">\n");
      if (Nunjucks.truthy(Nunjucks.get(message, "headingHtml"))
          || Nunjucks.truthy(Nunjucks.get(message, "headingText"))) {
        out.append("        <h2 class=\"govuk-cookie-banner__heading govuk-heading-m\">\n");
        out.append("          ")
            .append(Nunjucks.contentIndent(message, "headingHtml", "headingText", 10))
            .append('\n');
        out.append("        </h2>\n");
      }
      out.append("        <div class=\"govuk-cookie-banner__content\">\n");
      Object html = Nunjucks.get(message, "html");
      Object text = Nunjucks.get(message, "text");
      if (Nunjucks.truthy(html)) {
        out.append("          ")
            .append(Nunjucks.indent(Nunjucks.trim(Nunjucks.str(html)), 10, false))
            .append('\n');
      } else if (Nunjucks.truthy(text)) {
        out.append("          <p class=\"govuk-body\">")
            .append(Nunjucks.out(text))
            .append("</p>\n");
      }
      out.append("        </div>\n      </div>\n    </div>\n\n");

      var actions = Nunjucks.items(Nunjucks.get(message, "actions"));
      // Upstream macros treat a missing `actions` as absent, but an empty array still
      // opens the button group. Only render when the option is present as a list.
      Object actionsRaw = Nunjucks.get(message, "actions");
      if (actionsRaw instanceof java.util.List) {
        out.append("    <div class=\"govuk-button-group\">\n");
        for (Object action : actions) {
          out.append("      ")
              .append(Nunjucks.indent(Nunjucks.trim(cookieBannerAction(action)), 6, false))
              .append('\n');
        }
        out.append("    </div>\n");
      }

      out.append("\n  </div>\n");
    }

    out.append("</div>");
    return out.toString();
  }

  private static String cookieBannerAction(Object action) {
    Object href = Nunjucks.get(action, "href");
    if (!Nunjucks.truthy(href) || "button".equals(Nunjucks.str(Nunjucks.get(action, "type")))) {
      return ComponentsButton.renderButton(
          Params.of(
              "text", Nunjucks.get(action, "text"),
              "type", Nunjucks.defTruthy(Nunjucks.get(action, "type"), "button"),
              "name", Nunjucks.get(action, "name"),
              "value", Nunjucks.get(action, "value"),
              "classes", Nunjucks.get(action, "classes"),
              "href", href,
              "attributes", Nunjucks.get(action, "attributes")));
    }
    return "<a class=\"govuk-link"
        + Attributes.classesIf(Nunjucks.get(action, "classes"))
        + "\" href=\""
        + Nunjucks.out(href)
        + '"'
        + Attributes.attributes(Nunjucks.get(action, "attributes"))
        + ">"
        + Nunjucks.out(Nunjucks.get(action, "text"))
        + "</a>";
  }
}
