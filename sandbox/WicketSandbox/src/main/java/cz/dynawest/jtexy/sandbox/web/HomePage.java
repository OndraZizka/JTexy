package cz.dynawest.jtexy.sandbox.web;

import cz.dynawest.jtexy.JTexy;
import cz.dynawest.jtexy.TexyException;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.PropertyModel;

/**
 * Homepage
 */
public class HomePage extends WebPage {

    private static final long serialVersionUID = 1L;
    Form form;
    TextArea ta;
    private String textAreaContent = "Text to be formatted.";
    Label resultDiv = new Label("result", new PropertyModel(this, "labelText")) {{ setOutputMarkupId(true); }};
    private String labelText = "original";

    
    /**
     * Constructor that is invoked when page is invoked without a session.
     */
    public HomePage(final PageParameters parameters) {

        this.form = new Form("form");

        this.ta = new TextArea("text", new PropertyModel(this, "textAreaContent"));
        this.ta.setOutputMarkupId(true);
        this.ta.add(new AjaxEventBehavior("onKeyUp") {
            protected void onEvent(AjaxRequestTarget target) {
                //System.out.println( "Ajax! TA getModelObjectAsString(): "+ta.getModelObjectAsString() );
                System.out.println("Ajax! TA getModelObjectAsString(): " + ta.getModelObject().toString());

                //labelText = "Foobar.";
                labelText = textAreaContent;
                target.addComponent(resultDiv);
            }
        });

        this.ta.add(new AbstractAutoCompleteBehavior() {
            protected void onRequest(String string, RequestCycle rc) {
                
            }
        });

        form.add(ta);
        form.add(resultDiv);
        add(form);

    }// const

    
    /**
     *  Process the text with Texy.
     */
    public String processText(String text) {
        try {
            JTexy texy = JTexy.create();
            return texy.process(text);
        } catch (TexyException ex) {
            ex.printStackTrace();
            return ex.getMessage();
        }
    }
}
