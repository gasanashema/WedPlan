package rw.ac.auca.wedplan.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.Calendar;
import java.util.Date;

/**
 * Validation Type #3: Custom JSF Validator.
 * Ensures the wedding task deadline is within reasonable operational bounds (not more than 2 years in advance).
 */
@FacesValidator("taskDeadlineValidator")
public class TaskDeadlineValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null) {
            return;
        }

        if (!(value instanceof Date)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Invalid Date Format", "The value provided is not a valid date.");
            throw new ValidatorException(msg);
        }

        Date selectedDate = (Date) value;

        Calendar maxAllowedDate = Calendar.getInstance();
        maxAllowedDate.add(Calendar.YEAR, 2);

        if (selectedDate.after(maxAllowedDate.getTime())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Deadline Too Far in Future", "Wedding task deadline cannot be scheduled more than 2 years in advance.");
            throw new ValidatorException(msg);
        }
    }
}
