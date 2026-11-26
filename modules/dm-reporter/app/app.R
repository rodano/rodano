#############################################################################
# Options, dependencies and global code
#############################################################################

# DEPENDENCIES: LIBRARIES
library(shiny)
library(shinyjs)
library(bslib)
library(writexl)
library(readr)
library(DT)
library(httr)
library(readxl)
library(pbapply)
library(reshape2)
library(ggplot2)
library(rodano)

URLAPI <- Sys.getenv(
  x = "API_URL",
  unset = "http://localhost:8080"
) # rodano api address and port
print(sprintf("Found Rodano API location (%s) from %s", URLAPI, ifelse(Sys.getenv(x = "API_URL") == "", "default value", "environment variable")))
URL_RODANO <- ifelse(URLAPI == "http://localhost:8080", "http://localhost:7586", "/")
assign("CUSTOM_REPORTS_PATH", Sys.getenv(
  x = "CUSTOM_REPORTS_PATH",
  unset = ifelse(
    file.exists("study_path.cfg"),
    read_file(file = "study_path.cfg"),
    "/opt/custom-reports"
  )
))
print(sprintf("Found Custom reports path (%s)", get("CUSTOM_REPORTS_PATH")))


# OPTIONS: SHINY APP
SIDE_PAN_SIZE_HALF <- 5
SIDE_PAN_SIZE_FULL <- 12

MODULES <- list(
  "module_data_exports.R" = list(activated = TRUE, feature = "REPORTER_MODULE_DATA_EXPORTS"),
  "module_custom_reports.R" = list(activated = FALSE, feature = "REPORTER_MODULE_CUSTOM_REPORTS"),
  "module_tables_and_listings.R" = list(activated = FALSE, feature = "REPORTER_MODULE_TABLES_LISTINGS"),
  "module_graphical_reports.R" = list(activated = FALSE, feature = "REPORTER_MODULE_GRAPHICAL_REPORTS")
)
login_reporter_feature <- "LOGIN_REPORTER"

print("Loading study-specific reports:")
toSource <- list.files(get("CUSTOM_REPORTS_PATH"), pattern = "*\\.R") # Get all R code of custom reports
sapply(toSource, function(f_) {
  print(f_)
  if (f_ %in% names(MODULES)) MODULES[[f_]]$activated <<- TRUE # If it is one of the pre-defined modules, activate it
  source(sprintf("%s/%s", get("CUSTOM_REPORTS_PATH"), f_)) # Source R file to source
})
print("Study-specific reports loaded.")

# GLOBAL CODE
PUBLIC_CONFIG <- NA

# GLOBAL CODE: HELPER FUNCTIONS
getExportableDatasetModels <- function(url, auth) {
  # Get config and derive useful data from it
  dsModels <- get_config(url, auth)$datasetModels # Get all datasetModels from config through rodano api
  dsModels <- dsModels[sapply(dsModels, function(d) d$exportable)] # Extract only exportable dsModels
  namedDsModels <- sapply(dsModels, function(d) {
    named <- list()
    named[[d$shortname$en]] <- d$id
    return(named)
  }) # Return results under the form of a names list: "shortname: id" (name of elements will be used to display selectInput choices)
  return(namedDsModels)
}
# Set connection data related to current user
setUserConnectionData <- function(token, session) {
  session$userData$token <- token
  session$userData$auth <- create_authentication(list(Token = token))
}

# Check features from configuration for current user's profile
getUserFeaturesFromConfig <- function(userDTO) {
  # Filter only enabled roles
  enabled <- sapply(userDTO$roles, function(x) x$status == "ENABLED")
  enabledRoles <- userDTO$roles[enabled]

  # Extract features from enabled roles, maintaining the nested profile structure
  features <- lapply(enabledRoles, function(role) {
    if (!is.null(role$profile) && !is.null(role$profile$features)) {
      return(role$profile$features)
    }
    return(NULL)
  })

  # Flatten the list of features into a single vector
  allFeatures <- unlist(features)
  return(unlist(allFeatures))
}
# Set features associated to current user
setUserFeatures <- function(features, session) {
  session$userData$features <- features
}

# Check if user session is active
getUserSessionActive <- function(urlapi, auth) {
  connected_user <- tryCatch(
    get_connected_user_dto(urlapi, auth),
    error = function(e) {
      warning(
        sprintf("Get connected user DTO failed: %s", conditionMessage(e)),
        call. = FALSE
      )
      NULL
    }
  )

  !is.null(connected_user)
}

#############################################################################
# UI
#############################################################################

renderedUi <- function(input, output, session) {
  if (is.na(PUBLIC_CONFIG)) {
    print("Getting public config...")
    assign("PUBLIC_CONFIG", get_public_config(URLAPI))
    assign("STUDY_ABBR", PUBLIC_CONFIG$shortname[[PUBLIC_CONFIG$defaultLanguage$id]])
    assign("COPYRIGHT", PUBLIC_CONFIG$copyright)
    if (!is.null(PUBLIC_CONFIG$color) && nzchar(PUBLIC_CONFIG$color)) {
      session$setCurrentTheme(
        bs_theme(
          version = 5,
          bootswatch = "materia",
          primary = PUBLIC_CONFIG$color
        )
      )
    }
    print("Public config loaded.")
  }

  return(renderUI({
    list(
      # LOGIN PAGE
      if (!session$userData$connected) {
        list(
          page_navbar(
            title = HTML(PUBLIC_CONFIG$logo),
            navbar_options = navbar_options(
              bg = PUBLIC_CONFIG$color,
              collapsible = FALSE
            ),
            fillable = FALSE
          ),
          fluidRow(
            column(4,
              offset = 8,
              card(
                card_body(
                  h4(sprintf("%s reporting platform", STUDY_ABBR)),
                  helpText(sprintf("Please sign-in with your regular %s credentials.", STUDY_ABBR)),
                  textInput(
                    inputId = "emailInput",
                    label = "",
                    placeholder = "E-mail"
                  ),
                  passwordInput(
                    inputId = "passwordInput",
                    label = "",
                    placeholder = "Password",
                    value = ""
                  ),
                  actionButton(
                    inputId = "connectButton",
                    label = "Login",
                    class = "btn-primary"
                  ),
                  br(),
                  p("To recover your password, please go back to the ", a("main platform", href = URL_RODANO), "and click on ", em("I forgot my password"),
                    class = "help-text-small"
                  )
                )
              ),
            )
          ),
          tags$footer(p(COPYRIGHT),
            align = "center",
            class = "app-footer"
          )
        )
      } else {
        # APPLICATION MAIN PAGE
        list(
          page_navbar(
            title = tagList(
              HTML(PUBLIC_CONFIG$logo),
              actionButton(
                inputId = "logoutButton",
                label = tags$span(
                  class = "material-symbols-outlined",
                  "logout"
                ),
                class = "logout-button"
              )
            ),
            id = "content_tabs",
            navbar_options = navbar_options(
              bg = PUBLIC_CONFIG$color,
              collapsible = FALSE
            ),
            fillable = FALSE,
            # CUSTOM REPORTS
            nav_panel(
              "Custom Reports", verbatimTextOutput("summary"),
              if (MODULES[["module_custom_reports.R"]]$activated & MODULES[["module_custom_reports.R"]]$feature %in% session$userData$features) {
                fluidRow(
                  column(
                    SIDE_PAN_SIZE_HALF,
                    br(),
                    card(
                      card_body(
                        div(h5("Select desired Custom Report:"),
                          class = "panel-title"
                        ),
                        fluidRow(
                          class = "selection-row",
                          div(
                            class = "selection-field",
                            # NAME SELECTOR
                            selectInput(
                              inputId = "customReportSelector",
                              label = NULL,
                              choices = names(get("CUSTOM_REPORTS"))
                            )
                          ),
                          div(
                            class = "selection-download",
                            # DOWNLOAD CUSTOM REPORT BUTTON
                            downloadButton(
                              outputId = "generateCustomReport",
                              label = "",
                              class = "btn-primary"
                            )
                          ),
                        ),
                        # DESCRIPTION DISPLAY
                        div(h5(strong("Description")),
                          class = "description-block",
                          verbatimTextOutput(
                            outputId = "customReportDescription"
                          )
                        )
                      )
                    )
                  )
                )
              } else {
                column(
                  SIDE_PAN_SIZE_HALF,
                  br(),
                  h3(em("None implemented."))
                )
              }
            ),
            nav_panel(
              "Data Extracts",
              if (MODULES[["module_data_exports.R"]]$activated & MODULES[["module_data_exports.R"]]$feature %in% session$userData$features) {
                fluidRow(
                  column(
                    SIDE_PAN_SIZE_HALF,
                    br(),
                    # EXPORT SIDE PANEL
                    card(
                      card_body(
                        div(h5("Select an extract to download:"),
                          class = "panel-title"
                        ),
                        # EXPORT SELECTION
                        fluidRow(
                          class = "selection-row",
                          div(
                            class = "selection-field",
                            # NAME SELECTOR
                            selectInput(
                              inputId = "exportSelector",
                              label = NULL,
                              choices = getExportableDatasetModels(URLAPI, session$userData$auth)
                            )
                          ),
                          div(
                            class = "selection-download",
                            downloadButton(
                              outputId = "downloadSingleExport",
                              label = "",
                              class = "btn-primary"
                            )
                          ),
                        ),
                        div(HTML("<h6hr> OR </h6r>")),
                        # DOWNLOAD ALL BUTTON
                        downloadButton(
                          outputId = "downloadAllExports",
                          label = "Download All Exports",
                          class = "full-width-button btn-primary"
                        ),
                        br(),
                        # EXCEL FORMAT RADIO BUTTON
                        div(
                          class = "format-label",
                          h5("Format:")
                        ),
                        div(
                          class = "format-radio",
                          radioButtons(
                            inputId = "exportFormat",
                            label = NULL,
                            choices = c("Excel", "CSV"),
                            selected = "Excel",
                            inline = TRUE
                          )
                        )
                      )
                    )
                  )
                )
              } else {
                column(
                  SIDE_PAN_SIZE_HALF,
                  br(),
                  h3(em("None implemented."))
                )
              }
            ),

            # Tables & Listings
            nav_panel(
              "Tables & Listings",
              if (MODULES[["module_tables_and_listings.R"]]$activated & MODULES[["module_tables_and_listings.R"]]$feature %in% session$userData$features) {
                fluidRow(
                  column(
                    SIDE_PAN_SIZE_HALF,
                    br(),
                    card(
                      card_body(
                        div(h5("Select desired Tables / Listings:"),
                          class = "panel-title"
                        ),
                        fluidRow(
                          class = "selection-row",
                          div(
                            class = "selection-field",
                            # NAME SELECTOR
                            selectInput(
                              inputId = "tablesListingsSelector",
                              label = NULL,
                              choices = names(get("TABLES_AND_LISTINGS"))
                            )
                          ),
                          div(
                            class = "selection-download",
                            # DOWNLOAD CUSTOM REPORT BUTTON
                            downloadButton(
                              outputId = "generateTablesListings",
                              label = "",
                              class = "btn-primary"
                            )
                          )
                        ),
                        # DESCRIPTION DISPLAY
                        div(h5(strong("Description")),
                          class = "description-block",
                          verbatimTextOutput(
                            outputId = "tablesListingsDescription"
                          )
                        )
                      )
                    )
                  )
                )
              } else {
                column(
                  SIDE_PAN_SIZE_HALF,
                  br(),
                  h3(em("None implemented."))
                )
              }
            ),
            # Graphical reports
            nav_panel(
              "Graphical Reports",
              if (MODULES[["module_graphical_reports.R"]]$activated & MODULES[["module_graphical_reports.R"]]$feature %in% session$userData$features) {
                fluidRow(
                  br(),
                  column(
                    SIDE_PAN_SIZE_HALF,
                    card(
                      card_body(
                        div(h5("Select desired graphical report:"),
                          class = "panel-title"
                        ),
                        fluidRow(
                          class = "selection-row",
                          div(
                            class = "selection-field",
                            # NAME SELECTOR
                            selectInput(
                              inputId = "graphicalReportSelector",
                              label = NULL,
                              choices = c("--", names(get("GRAPHICAL_REPORTS")))
                            )
                          )
                        ),
                        # DESCRIPTION DISPLAY
                        div(h5(strong("Description")),
                          class = "description-block",
                          verbatimTextOutput(
                            outputId = "graphicalReportDescription"
                          )
                        )
                      )
                    ),
                  ),
                  column(12 - SIDE_PAN_SIZE_HALF - 6),
                  column(
                    6,
                    plotOutput(
                      outputId = "graphPlotOutput", # TODO: hover option?
                      width = "100%",
                      height = "200px"
                    ),
                    br(),
                    # DOWNLOAD PLOT BUTTON
                    downloadButton(outputId = "downloadPlot", label = "Download plot", class = "btn-primary"),
                    br(),
                    hr(),
                    br(),
                    div(dataTableOutput(outputId = "graphDataOutput"),
                      class = "scroll-table"
                    ),
                    br(),
                    # DOWNLOAD DATA BUTTON
                    downloadButton(outputId = "downloadData", label = "Download data", class = "btn-primary"),
                    br(),
                    br()
                  )
                )
              } else {
                column(
                  SIDE_PAN_SIZE_HALF,
                  br(),
                  h3(em("None implemented."))
                )
              }
            )
          )
        )
      }
    )
  }))
}

ui <- fluidPage(
  includeCSS("www/custom.css"),
  theme = bs_theme(
    version = 5,
    bootswatch = "materia",
    primary = "#0d6efd"
  ),
  tags$head(
    tags$title("Rodano Report App"),
    tags$link(
      rel = "stylesheet",
      href = "https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined"
    )
  ),
  useShinyjs(),
  extendShinyjs(script = "script.js", functions = c("setTokenToLocalStorage")),
  uiOutput(outputId = "widget")
)


#############################################################################
# SERVER
#############################################################################

server <- shinyServer(function(input, output, session) {
  session$userData$connected <- FALSE
  tokenFromLocalStorage <- reactiveVal(value = NULL)
  graphComponents <- reactiveValues()

  ##### MODULE: EXPORTS #####

  # UNUSED: Preview selected export
  observeEvent(input$previewExport, {
    withProgress(message = "Getting Export to display...", {
      setProgress(1 / 3)
      export <- get_extract(URLAPI, session$userData$auth, input$exportSelector)
      setProgress(2 / 3)
      output$exportTable <- DT::renderDataTable(export, options = list(scrollX = TRUE))
      setProgress(3 / 3)
    })
  })

  # Download selected export
  output$downloadSingleExport <- downloadHandler(
    content = function(file) {
      withProgress(message = "The Export is being processed. Please wait", {
        setProgress(1 / 3)
        export <- get_extract(URLAPI, session$userData$auth, input$exportSelector, as_character = TRUE)
        setProgress(2 / 3)
        if (input$exportFormat == "Excel") {
          write_xlsx(export, file)
        } else {
          write.table(export, file, sep = ",", row.names = FALSE, col.names = TRUE, qmethod = "double")
        }
        setProgress(3 / 3)
      })
    },
    filename = function() {
      paste(input$exportSelector, ifelse(input$exportFormat == "Excel", "xlsx", "csv"), sep = ".")
    }
  )

  # Download all exports
  output$downloadAllExports <- downloadHandler(
    content = function(file) {
      withProgress(message = "The Exports are being processed. Please wait", {
        studyExtracts <- getExportableDatasetModels(URLAPI, session$userData$auth)
        n <- length(studyExtracts)
        convert <- input$exportFormat == "Excel"
        # Go to a temp dir to avoid permission issues
        owd <- setwd(tempdir())
        on.exit(setwd(owd))
        files <- NULL
        setProgress(0)
        # Loop through documents
        for (i in 1:n) {
          e <- get_extract(URLAPI, session$userData$auth, studyExtracts[i])
          if (convert) {
            eName <- paste(studyExtracts[i], "xlsx", sep = ".")
            write_xlsx(e, eName)
          } else {
            eName <- paste(studyExtracts[i], "csv", sep = ".")
            write.table(e, eName, sep = ",", row.names = FALSE, col.names = TRUE, qmethod = "double")
          }
          files <- c(files, eName)
          setProgress(i / (n + 1))
        }
        zip(
          zipfile = file,
          files = files
        )
        setProgress(n + 1)
      })
    },
    filename = function() {
      paste0("Exports_", STUDY, ".zip", collapse = "")
    },
    contentType = "application/zip"
  )

  ##### MODULE: CUSTOM REPORTS #####

  # Download selected custom report
  output$generateCustomReport <- downloadHandler(
    content = function(file) {
      withProgress(message = "The Custom report is being generated. Please wait", {
        # Go to a temp dir to avoid permission issues
        owd <- setwd(tempdir())
        on.exit(setwd(owd))
        setProgress(1 / 3)
        # Get reports
        reports <- getCustomReport(URLAPI, session$userData$auth, input$customReportSelector)
        setProgress(2 / 3)
        write_xlsx(reports, file, format_headers = TRUE)
        setProgress(3 / 3)
      })
    },
    filename = function() {
      paste0(STUDY, "_", gsub("[ .]", "_", input$customReportSelector), ".xlsx")
    }
  )

  output$customReportDescription <- renderText({
    if (input$customReportSelector %in% names(CUSTOM_REPORTS)) {
      return(CUSTOM_REPORTS[[input$customReportSelector]])
    } else {
      return("[UNAVAILABLE]")
    }
  })

  ##### MODULE: TABLES & LISTINGS #####
  output$generateTablesListings <- downloadHandler(
    content = function(file) {
      withProgress(message = "The Tables / Listings are being generated. Please wait", {
        # Go to a temp dir to avoid permission issues
        owd <- setwd(tempdir())
        on.exit(setwd(owd))
        setProgress(1 / 3)
        # Get reports
        reports <- getTablesListings(URLAPI, session$userData$auth, input$tablesListingsSelector)
        setProgress(2 / 3)
        write_xlsx(reports, file, format_headers = TRUE)
        setProgress(3 / 3)
      })
    },
    filename = function() {
      paste0(STUDY, "_", gsub("[ .]", "_", input$tablesListingsSelector), ".xlsx")
    }
  )

  output$tablesListingsDescription <- function() {
    if (input$tablesListingsSelector %in% names(get("TABLES_AND_LISTINGS"))) {
      return(get("TABLES_AND_LISTINGS")[[input$tablesListingsSelector]])
    } else {
      return("[UNAVAILABLE]")
    }
  }

  ##### MODULE: GRAPHICAL REPORTS #####

  observeEvent(input$graphicalReportSelector, {
    output$graphicalReportDescription <- renderText({
      if (input$graphicalReportSelector %in% names(get("GRAPHICAL_REPORTS"))) {
        return(get("GRAPHICAL_REPORTS")[[input$graphicalReportSelector]])
      } else {
        return("")
      }
    })

    if (input$graphicalReportSelector != "--") {
      withProgress(message = "The Graphical report is being generated. Please wait", {
        setProgress(1 / 3)
        # Get full graphical report
        graphComponents <- makeGraphicalReport(URLAPI, session$userData$auth, input$graphicalReportSelector)
        setProgress(2 / 3)
        # Retrieve data and plot in dedicated variables
        output$graphDataOutput <- renderDataTable(graphComponents$table,
          options = list(scrollX = TRUE, lengthMenu = c(5, 30, 50), pageLength = 5),
          rownames = FALSE
        )
        output$graphPlotOutput <- renderPlot({
          graphComponents$plot
        })
        setProgress(3 / 3)
      })

      output$downloadPlot <- downloadHandler(
        filename = function() {
          paste(input$graphicalReportSelector, "png", sep = ".")
        },
        content = function(file) {
          ggsave(file, plot = graphComponents$plot)
        }
      )

      output$downloadData <- downloadHandler(
        filename = function() {
          paste(input$graphicalReportSelector, "csv", sep = ".")
        },
        content = function(file) {
          write.csv(graphComponents$table, file, row.names = FALSE)
        }
      )
    }
  })

  ##### Connection stuff #####

  # Check presence of token in local storage from js
  observeEvent(input$token, {
    tokenFromLocalStorage(input$token)
  })

  # Check presence of token on token in local storage change
  observeEvent(tokenFromLocalStorage(), {
    setUserConnectionData(tokenFromLocalStorage(), session)
    if (getUserSessionActive(URLAPI, session$userData$auth)) {
      session$userData$dto <- get_connected_user_dto(URLAPI, session$userData$auth)
      features <- getUserFeaturesFromConfig(session$userData$dto)
      setUserFeatures(features, session)
      session$userData$connected <- TRUE
    } else {
      session$userData$connected <- FALSE
    }

    output$widget <- renderedUi(input, output, session)
  })

  # User active disconnection
  observeEvent(input$logoutButton, {
    logout_user(URLAPI, session$userData$auth)
    session$userData$connected <- FALSE
    tokenFromLocalStorage("NULL")
  })

  # User connection
  observeEvent(input$connectButton, {
    tryCatch(
      {
        # Get temporary token and authentication for this user
        token_temp <- get_connection_token(URLAPI, input$emailInput, input$passwordInput)
        auth_temp <- create_authentication(list(Token = token_temp))
        # Get user's DTO object
        session$userData$dto <- get_connected_user_dto(
          URLAPI,
          auth_temp
        )
        features <- getUserFeaturesFromConfig(session$userData$dto)
        setUserFeatures(features, session)

        if (login_reporter_feature %in% session$userData$features) {
          token <- token_temp
          setUserConnectionData(token, session)
          js$setTokenToLocalStorage(token)
          tokenFromLocalStorage(token)
        } else {
          logout_user(URLAPI, auth_temp)
          stop("Access to reporter is not granted for this user.", call. = FALSE)
        }
      },
      error = function(e) {
        showNotification(sprintf("%s", e),
          type = "error"
        )
        tokenFromLocalStorage("null")
      }
    )
  })
})


#############################################################################
# Run the app
#############################################################################

shinyApp(ui = ui, server = server)
