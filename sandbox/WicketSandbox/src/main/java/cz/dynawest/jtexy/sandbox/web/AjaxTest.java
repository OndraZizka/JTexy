
package cz.dynawest.jtexy.sandbox.web;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.IClusterable;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormValidatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;

/**
 * Page to demonstrate instant ajax validaion feedback. Validation is triggered as the user is
 * typing, but is throttled so that only one ajax call is made to the server per second.
 *
 * @author Igor Vaynberg (ivaynberg)
 */
public class AjaxTest extends WebPage
{
    private final Bean bean = new Bean();

    /**
     * Constructor
     */
    public AjaxTest()
    {
        // create feedback panel to show errors
        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        // add form with markup id setter so it can be updated via ajax
        Form<Bean> form = new Form<Bean>("form", new CompoundPropertyModel<Bean>(bean));
        add(form);
        form.setOutputMarkupId(true);

        FormComponent fc;

        // add form components to the form as usual

        fc = new RequiredTextField<String>("name");
        fc.add(StringValidator.minimumLength(4));
        fc.setLabel(new ResourceModel("label.name"));

        form.add(fc);
        form.add(new SimpleFormComponentLabel("name-label", fc));

        fc = new RequiredTextField<String>("email");
        fc.add(EmailAddressValidator.getInstance());
        fc.setLabel(new ResourceModel("label.email"));

        form.add(fc);
        form.add(new SimpleFormComponentLabel("email-label", fc));

        // attach an ajax validation behavior to all form component's onkeydown
        // event and throttle it down to once per second

        AjaxFormValidatingBehavior.addToAllFormComponents(form, "onkeyup", Duration.ONE_SECOND);

        // add a button that can be used to submit the form via ajax
        form.add(new AjaxButton("ajax-button", form)
        {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form)
            {
                // repaint the feedback panel so that it is hidden
                target.addComponent(feedback);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form)
            {
                // repaint the feedback panel so errors are shown
                target.addComponent(feedback);
            }
        });
    }

    /** simple java bean. */
    public static class Bean implements IClusterable
    {
        private String name, email;

        public String getEmail()        {            return email;        }
        public void setEmail(String email)        {            this.email = email;        }
        public String getName()        {            return name;        }
        public void setName(String name)        {            this.name = name;        }
    }
}
