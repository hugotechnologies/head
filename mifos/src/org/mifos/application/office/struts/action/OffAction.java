package org.mifos.application.office.struts.action;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.mifos.application.customer.business.CustomFieldDefinitionEntity;
import org.mifos.application.customer.business.CustomFieldView;
import org.mifos.application.customer.util.helpers.CustomerConstants;
import org.mifos.application.master.business.service.MasterDataService;
import org.mifos.application.office.business.OfficeBO;
import org.mifos.application.office.business.service.OfficeBusinessService;
import org.mifos.application.office.exceptions.OfficeException;
import org.mifos.application.office.struts.actionforms.OffActionForm;
import org.mifos.application.office.util.helpers.OfficeLevel;
import org.mifos.application.office.util.helpers.OfficeStatus;
import org.mifos.application.office.util.helpers.OperationMode;
import org.mifos.application.office.util.resources.OfficeConstants;
import org.mifos.application.util.helpers.ActionForwards;
import org.mifos.application.util.helpers.CustomFieldType;
import org.mifos.application.util.helpers.EntityType;
import org.mifos.framework.business.service.BusinessService;
import org.mifos.framework.business.service.ServiceFactory;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.hibernate.helper.HibernateUtil;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.struts.action.BaseAction;
import org.mifos.framework.struts.tags.DateHelper;
import org.mifos.framework.util.helpers.BusinessServiceName;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.StringUtils;

public class OffAction extends BaseAction {

	@Override
	protected BusinessService getService() throws ServiceException {
		return ServiceFactory.getInstance().getBusinessService(
				BusinessServiceName.Office);
	}

	@Override
	protected boolean skipActionFormToBusinessObjectConversion(String method) {
		return true;
	}

	public ActionForward load(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		OffActionForm actionForm = (OffActionForm) form;
		loadParents(request, actionForm);
		actionForm.clear();
		loadCreateCustomFields(actionForm, request);
		loadofficeLevels(request);
		return mapping.findForward(ActionForwards.load_success.toString());
	}

	public ActionForward loadParent(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		OffActionForm offActionForm =(OffActionForm) form ;
		loadParents(request,offActionForm );
		if (offActionForm.getInput()!=null&&offActionForm.getInput().equals("edit"))
			return mapping.findForward(ActionForwards.edit_success.toString());
		else
			return mapping.findForward(ActionForwards.load_success.toString());
	}

	public ActionForward preview(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		return mapping.findForward(ActionForwards.preview_success.toString());
	}

	public ActionForward previous(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		return mapping.findForward(ActionForwards.previous_success.toString());
	}

	public ActionForward create(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		OffActionForm offActionForm = (OffActionForm) form;
		OfficeLevel level = OfficeLevel
				.getOfficeLevel(getShortValue(offActionForm.getOfficeLevel()));
		OfficeBO parentOffice = ((OfficeBusinessService) getService())
				.getOffice(getShortValue(offActionForm.getParentOfficeId()));

		OfficeBO officeBO = new OfficeBO(getUserContext(request), level,
				parentOffice, offActionForm.getCustomFields(), offActionForm
						.getOfficeName(), offActionForm.getShortName(),
				offActionForm.getAddress(), OperationMode.REMOTE_SERVER);
		HibernateUtil.closeandFlushSession();
		officeBO.save();
		SessionUtils.setAttribute(Constants.BUSINESS_KEY, officeBO, request
				.getSession());
		return mapping.findForward(ActionForwards.create_success.toString());
	}
	
	public ActionForward getAllOffices(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		UserContext userContext = (UserContext) SessionUtils.getAttribute(
				Constants.USER_CONTEXT_KEY, request.getSession());
		List<OfficeBO> officeList=getOffices(userContext, ((OfficeBusinessService) getService()).getOfficesTillBranchOffice());
		SessionUtils.setAttribute(OfficeConstants.GET_HEADOFFICE, getOffice(officeList,OfficeLevel.HEADOFFICE), request.getSession());
		SessionUtils.setAttribute(OfficeConstants.GET_REGIONALOFFICE, getOffice(officeList,OfficeLevel.REGIONALOFFICE), request.getSession());
		SessionUtils.setAttribute(OfficeConstants.GET_SUBREGIONALOFFICE, getOffice(officeList,OfficeLevel.SUBREGIONALOFFICE), request.getSession());
		SessionUtils.setAttribute(OfficeConstants.GET_AREAOFFICE, getOffice(officeList,OfficeLevel.AREAOFFICE), request.getSession());
		SessionUtils.setAttribute(OfficeConstants.GET_BRANCHOFFICE, getOffices(
				userContext, ((OfficeBusinessService) getService())
						.getBranchOffices()), request.getSession());
		return mapping.findForward(ActionForwards.search_success.toString());
	}
	
	
	private List<OfficeBO> getOffice(List<OfficeBO> officeList, OfficeLevel officeLevel) throws OfficeException{
		if(officeList!=null){
			List<OfficeBO> newOfficeList=new ArrayList<OfficeBO>();
			for(OfficeBO officeBO : officeList){
				if(officeBO.getOfficeLevel().equals(officeLevel)){
					newOfficeList.add(officeBO);
				}
			}
			if(newOfficeList.isEmpty())
				return null;
			return newOfficeList;
		}
		return null;	
	}
	
	private List<OfficeBO> getOffices(UserContext userContext,List<OfficeBO> officeList) throws Exception{
		if(officeList!=null){
			for(OfficeBO officeBO : officeList){
				officeBO.getLevel().setLocaleId(userContext.getLocaleId());
				officeBO.getStatus().setLocaleId(userContext.getLocaleId());
			}
		}
		return officeList;
	}

	private void loadParents(HttpServletRequest request, OffActionForm form)
			throws Exception {
		String officeLevel = (String) request.getParameter("officeLevel");
		if (!StringUtils.isNullOrEmpty(officeLevel)) {
			form.setOfficeLevel(officeLevel);
			OfficeLevel Level = OfficeLevel.getOfficeLevel(Short
					.valueOf(officeLevel));
			SessionUtils.setAttribute(OfficeConstants.PARENTS,
					((OfficeBusinessService) getService()).getActiveParents(
							Level, getUserContext(request).getLocaleId()),
					request.getSession());
		}
	}

	private void loadCreateCustomFields(OffActionForm actionForm,
			HttpServletRequest request) throws SystemException, ApplicationException {
		loadCustomFieldDefinitions(request);
		// Set Default values for custom fields
		List<CustomFieldDefinitionEntity> customFieldDefs = (List<CustomFieldDefinitionEntity>) SessionUtils
				.getAttribute(CustomerConstants.CUSTOM_FIELDS_LIST, request
						.getSession());
		List<CustomFieldView> customFields = new ArrayList<CustomFieldView>();

		for (CustomFieldDefinitionEntity fieldDef : customFieldDefs) {
			if (StringUtils.isNullAndEmptySafe(fieldDef.getDefaultValue())
					&& fieldDef.getFieldType().equals(
							CustomFieldType.DATE.getValue())) {
				customFields.add(new CustomFieldView(fieldDef.getFieldId(),
						DateHelper.getUserLocaleDate(getUserContext(request)
								.getPereferedLocale(), fieldDef
								.getDefaultValue()), fieldDef.getFieldType()));
			} else {
				customFields.add(new CustomFieldView(fieldDef.getFieldId(),
						fieldDef.getDefaultValue(), fieldDef.getFieldType()));
			}
		}
		actionForm.setCustomFields(customFields);
	}

	private void loadCustomFieldDefinitions(HttpServletRequest request)
			throws SystemException, ApplicationException {
		MasterDataService masterDataService = (MasterDataService) ServiceFactory
				.getInstance().getBusinessService(
						BusinessServiceName.MasterDataService);
		List<CustomFieldDefinitionEntity> customFieldDefs = masterDataService
				.retrieveCustomFieldsDefinition(EntityType.OFFICE);
		SessionUtils.setAttribute(CustomerConstants.CUSTOM_FIELDS_LIST,
				customFieldDefs, request.getSession());
	}

	public ActionForward validate(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String method = (String) request.getAttribute("methodCalled");
		return mapping.findForward(method + "_failure");
	}

	public ActionForward get(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		OffActionForm actionForm = (OffActionForm) form;
		OfficeBO officeBO = null;
		if (StringUtils.isNullOrEmpty(actionForm.getOfficeId()))
			throw new OfficeException(OfficeConstants.KEYGETFAILED);
		officeBO = ((OfficeBusinessService) getService()).getOffice(Short
				.valueOf(actionForm.getOfficeId()));
		actionForm.clear();
		loadCreateCustomFields(actionForm, request);
		//loadofficeLevels(request);
		officeBO.getStatus().setLocaleId(getUserContext(request).getLocaleId());
		officeBO.getLevel().setLocaleId(getUserContext(request).getLocaleId());
		actionForm.populate(officeBO);
		SessionUtils.setAttribute(Constants.BUSINESS_KEY, officeBO, request
				.getSession());
		return mapping.findForward(ActionForwards.get_success.toString());
	}

	private void loadofficeLevels(HttpServletRequest request)
			throws ServiceException {
		SessionUtils.setAttribute(OfficeConstants.OFFICELEVELLIST,
				((OfficeBusinessService) getService())
						.getConfiguredLevels(getUserContext(request)
								.getLocaleId()), request.getSession());
	}
	private void loadOfficeStatus(HttpServletRequest request)
			throws ServiceException{
		SessionUtils.setAttribute(OfficeConstants.OFFICESTATUSLIST,((OfficeBusinessService)getService()).getStatusList(getUserContext(request).getLocaleId()), request.getSession());
		
	}
	public ActionForward edit(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		OffActionForm offActionForm= (OffActionForm)form;
		loadofficeLevels(request);
		loadParents(request,offActionForm);
		loadCreateCustomFields(offActionForm, request);
		loadOfficeStatus(request);
		return mapping.findForward(ActionForwards.edit_success.toString());
	}
	public ActionForward editpreview(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return mapping.findForward(ActionForwards.editpreview_success.toString());
	}
	public ActionForward editprevious(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return mapping.findForward(ActionForwards.editprevious_success.toString());
	}
	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		OffActionForm offActionForm = (OffActionForm)form;
		OfficeBO sessionOffice = (OfficeBO) SessionUtils.getAttribute(Constants.BUSINESS_KEY,request.getSession());
		
		HibernateUtil.closeandFlushSession();
		OfficeBO office = ((OfficeBusinessService) getService()).getOffice(Short
				.valueOf(sessionOffice.getOfficeId()));
		office.setVersionNo(sessionOffice.getVersionNo());
		
		office.setUserContext(getUserContext(request));
		
		OfficeStatus newStatus=OfficeStatus.ACTIVE;
		if(getShortValue(offActionForm.getOfficeStatus())!=null)
		 newStatus = OfficeStatus.getOfficeStatus(getShortValue(offActionForm.getOfficeStatus()));
		OfficeLevel newlevel = OfficeLevel
		.getOfficeLevel(getShortValue(offActionForm.getOfficeLevel()));
		OfficeBO parentOffice=null;
		if(getShortValue(offActionForm.getParentOfficeId())!=null)
			parentOffice= ((OfficeBusinessService) getService())
		.getOffice(getShortValue(offActionForm.getParentOfficeId()));
		office.update(offActionForm.getOfficeName(),offActionForm.getShortName(),newStatus,newlevel,parentOffice,offActionForm.getAddress(),offActionForm.getCustomFields());
		return mapping.findForward(ActionForwards.update_success.toString());
	}
}
