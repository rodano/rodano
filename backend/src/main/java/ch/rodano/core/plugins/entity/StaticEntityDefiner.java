package ch.rodano.core.plugins.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ch.rodano.configuration.model.feature.Feature;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.mail.CustomizedTemplatedMail;
import ch.rodano.core.model.mail.MailOrigin;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.model.rules.entity.StaticEntity;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeExtension;
import ch.rodano.core.model.user.User;
import ch.rodano.core.model.user.UserSearch;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.mail.MailService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.user.UserDAOService;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StaticEntityDefiner extends AbstractStaticEntityDefiner {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final StudyService studyService;
	private final UserDAOService userDAOService;
	private final MailService mailService;
	private final ActorService actorService;

	public StaticEntityDefiner(
		final StudyService studyService,
		final UserDAOService userDAOService,
		final MailService mailService,
		@Lazy final ActorService actorService
	) {
		this.studyService = studyService;
		this.userDAOService = userDAOService;
		this.mailService = mailService;
		this.actorService = actorService;
	}

	/**
	 * Get all registered identifiable entity beans
	 *
	 * @return Registered identifiable entity beans
	 */
	@Override
	public List<IdentifiableEntity> getRegisteredBeans() {
		return List.of(
			new StaticEntity() {
				@Override
				public void action(final Map<String, Object> parameters, final DatabaseActionContext context) {
					logger.info((String) parameters.get("TEXT"));
				}

				@Override
				public String getId() {
					return "LOG";
				}
			},
			new StaticEntity() {
				@SuppressWarnings("unchecked")
				@Override
				public void action(final Map<String, Object> parameters, final DatabaseActionContext context) {
					final List<Evaluable> scopes = parameters.containsKey("SCOPES") ? new ArrayList<>((Set<Evaluable>) parameters.get("SCOPES")) : Collections.emptyList();
					final List<Evaluable> events = parameters.containsKey("EVENTS") ? new ArrayList<>((Set<Evaluable>) parameters.get("EVENTS")) : Collections.emptyList();
					final List<Evaluable> forms = parameters.containsKey("FORMS") ? new ArrayList<>((Set<Evaluable>) parameters.get("FORMS")) : Collections.emptyList();
					final List<Evaluable> workflows = parameters.containsKey("WORKFLOWS") ? new ArrayList<>((Set<Evaluable>) parameters.get("WORKFLOWS")) : Collections.emptyList();
					final List<Evaluable> fields = parameters.containsKey("FIELDS") ? new ArrayList<>((Set<Evaluable>) parameters.get("FIELDS")) : Collections.emptyList();

					final var branchType = (String) parameters.get("BRANCH_TYPE");

					final var study = studyService.getStudy();

					//find all recipients for the e-mail
					final Feature feature = study.getFeature((String) parameters.get("FEATURE_ID"));

					//retrieve profiles having feature
					final var profileIds = study.getProfiles().stream().filter(profile -> profile.hasRight(feature)).map(Profile::getId).collect(Collectors.toSet());

					final Set<String> emails = new HashSet<>();

					if(!profileIds.isEmpty()) {

						final var search = new UserSearch()
							.enforceProfileIds(profileIds)
							.enforceEnabled(true);

						if(!"ALL".equals(branchType)) {
							//if branch type is not all, retrieve users from scopes and feature
							final List<Scope> recipientScopes = parameters.containsKey("RECIPIENTS_SCOPE") ? new ArrayList<>((Set<Scope>) parameters.get("RECIPIENTS_SCOPE")) : Collections.emptyList();

							search
								.enforceScopePks(recipientScopes.stream().map(Scope::getPk).collect(Collectors.toSet()))
								.enforceExtension(ScopeExtension.valueOf(branchType));
						}

						// Retrieve users
						final var users = userDAOService.search(search).getObjects();
						users.stream().map(User::getEmail).forEach(emails::add);
					}

					//retrieve e-mail parameters
					final var subject = (String) parameters.get("SUBJECT");
					final var contentText = (String) parameters.get("CONTENT_TEXT");
					final var contentHtml = (String) parameters.get("CONTENT_HTML");
					final var intent = (String) parameters.get("INTENT");

					final var mail = new CustomizedTemplatedMail(
						subject, contentText, contentHtml, Map.ofEntries(
							Map.entry("SCOPES", scopes),
							Map.entry("EVENTS", events),
							Map.entry("FORMS", forms),
							Map.entry("WORKFLOWS", workflows),
							Map.entry("FIELDS", fields),
							Map.entry("LANGUAGES", actorService.getLanguages(context.actor().orElse(null)))
						)
					);
					mail.setSender(study.getEmail());
					mail.setReplyTo(study.getEmail());
					mail.setRecipients(emails);
					mail.setOrigin(MailOrigin.RULE);
					mail.setIntent(intent != null && !intent.trim().isEmpty() ? intent : "Send email from static rule");

					mailService.createMail(mail, context, "Send email from static rule");

					scopes.stream().map(scope -> String.format("%s sent for %s", mail.getSubject(), ((Scope) scope).getCodeAndShortname())).forEach(logger::info);
				}

				@Override
				public String getId() {
					return "EMAIL";
				}
			}
		);
	}
}
