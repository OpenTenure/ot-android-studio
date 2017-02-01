package org.fao.sola.clients.android.opentenure.form.constraint;

import java.text.MessageFormat;

import org.fao.sola.clients.android.opentenure.form.FieldConstraint;
import org.fao.sola.clients.android.opentenure.form.FieldConstraintType;
import org.fao.sola.clients.android.opentenure.form.FieldPayload;
import org.fao.sola.clients.android.opentenure.form.FieldType;
import org.fao.sola.clients.android.opentenure.form.FieldValueType;

public class NotNullConstraint extends FieldConstraint {

	public NotNullConstraint(){
		super();
		fieldConstraintType=FieldConstraintType.NOT_NULL;
		addApplicableType(FieldType.DATE);
		addApplicableType(FieldType.DECIMAL);
		addApplicableType(FieldType.DOCUMENT);
		addApplicableType(FieldType.GEOMETRY);
		addApplicableType(FieldType.INTEGER);
		addApplicableType(FieldType.SNAPSHOT);
		addApplicableType(FieldType.TEXT);
		addApplicableType(FieldType.TIME);
		this.errorMsg = "Value of {0} is mandatory";
	}

	public NotNullConstraint(NotNullConstraint nnc){
		super(nnc);
	}

	@Override
	public boolean check(String externalDisplayName, FieldPayload fieldPayload) {
		displayErrorMsg = null;
		if(fieldPayload == null
				|| (fieldPayload.getBooleanPayload()==null && fieldPayload.getFieldValueType() == FieldValueType.BOOL)
				|| (fieldPayload.getBigDecimalPayload()==null && fieldPayload.getFieldValueType() == FieldValueType.NUMBER)
				|| (fieldPayload.getStringPayload()==null && fieldPayload.getFieldValueType() == FieldValueType.TEXT)){
			if(externalDisplayName != null){
				displayErrorMsg = MessageFormat.format(errorMsg, externalDisplayName);
			}else{
				displayErrorMsg = MessageFormat.format(errorMsg, displayName);
			}
			return false;
		}
		return true;
	}
}
