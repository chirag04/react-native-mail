export namespace Mailer {
  function mail(options: {
    subject?: string;
    recipients?: string[];
    ccRecipients?: string[];
    bccRecipients?: string[];
    body?: string;
    customChooserTitle?: string;
    isHTML?: boolean;
    attachments?: {
      path: string;
      type?: string;
      mimeType?: string;
      name?: string;
    }[]
  }, callback: (
    error: string,
    event?: string
  ) => void): void;
}

export default Mailer;
