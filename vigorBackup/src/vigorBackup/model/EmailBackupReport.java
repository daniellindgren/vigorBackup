package vigorBackup.model;

import java.util.List;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

/**
 * Responsible for the email reporting. This class builds a simple html table
 * with the downloaders results.
 */
public final class EmailBackupReport {
	/**
	 * This class can't be instantiated as there's no need for it.
	 */
	private EmailBackupReport() {
		
	}
	/**
	 * The separator that is used to split the "TO" addresses.
	 */
	private static final String EMAIL_SEPARATOR = ",";
	/**
	 * Maximum time to wait for a server response, in milliseconds.
	 */
	private static final int SMTP_TIMEOUT = 10000;

	/**
	 * Sends the backups report.
	 * 
	 * @param routersDownloaders
	 *            The list of downloaders to build the report.
	 */
	public static void sendBackupReport(
			final List<BaseRouterDownloader> routersDownloaders) {
		// TODO: treat auth not needed
		StringBuilder sb = new StringBuilder();
		sb.append("<table border=\"1\"><tr>" + "<th>Cliente</th>"
				+ "<th>Local</th>" + "<th>Backup OK?</th></tr>");

		routersDownloaders
				.forEach(downloader -> {
					sb.append("<tr><td>" + downloader.getRouter().getSiteName()
							+ "</td>");
					sb.append("<td>" + downloader.getRouter().getDescription()
							+ "</td>");

					String result = downloader.isBackupOK() ? ""
							+ "<font color=\"green\">OK!</font>"
							: "<font color=\"red\">Error</font>";
				
					sb.append("<td>" + result + "</td>");
					sb.append("</tr>");

				});

		sb.append("</table>");

		HtmlEmail email = new HtmlEmail();
		email.setDebug(LoadConfigFile.IS_SMTP_DEBUG_ON);
		email.setSocketConnectionTimeout(SMTP_TIMEOUT);
		email.setSocketTimeout(SMTP_TIMEOUT);
		email.setHostName(LoadConfigFile.SMTP_HOST);
		email.setSmtpPort(LoadConfigFile.SMTP_PORT);
		email.setSslSmtpPort(String.valueOf(LoadConfigFile.SMTP_PORT));
		email.setAuthenticator(new DefaultAuthenticator(
				LoadConfigFile.SMTP_LOGIN_USERNAME,
				LoadConfigFile.SMTP_PASSWORD));
		email.setSSL(LoadConfigFile.IS_SMTP_SSL_ENABLED);

		try {
			email.setFrom(LoadConfigFile.SMTP_FROM_EMAIL);
			email.setSubject("Routers backup report");
			email.setHtmlMsg(sb.toString());
			String[] emails = LoadConfigFile.SMTP_TO_EMAIL
					.split(EMAIL_SEPARATOR);
			for (int i = 0; i < emails.length; i++) {
				email.addTo(emails[i]);
			}
			email.send();
		} catch (EmailException e) {
			if (!LoadConfigFile.IS_SMTP_DEBUG_ON) {
				System.out
						.println("Could not send the e-mail. "
								+ "Active the debug to know why");
			} else {
				e.printStackTrace();
			}

		}

	}
}
