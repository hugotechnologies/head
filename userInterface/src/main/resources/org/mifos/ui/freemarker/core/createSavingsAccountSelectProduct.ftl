[#ftl]
[#--
* Copyright (c) 2005-2010 Grameen Foundation USA
*  All rights reserved.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
*  implied. See the License for the specific language governing
*  permissions and limitations under the License.
*
*  See also http://www.apache.org/licenses/LICENSE-2.0.html for an
*  explanation of the license and how it is applied.
--]

[@layout.webflow currentState="createSavingsAccount.flowState.enterAccountInfo" 
                 states=["createSavingsAccount.flowState.selectCustomer", 
                 		 "createSavingsAccount.flowState.enterAccountInfo", 
                 		 "createSavingsAccount.flowState.reviewAndSubmit"]] 

<h1>Create Savings account - <span class="standout">Enter Savings account information</span></h1>
<p>Select a Savings instance. Then click Continue. Click Cancel to return to Clients & Accounts without submitting information.</p>
<p>*Fields marked with an asterisk are required.</p>
<br/>

<p><span class="standout">Account Owner</span>: ${savingsAccountFormBean.customer.displayName}</p>
<br/>

<form action="${flowExecutionUrl}" method="post" class="two-columns">
	<fieldset>
	<div class="row">
		<label for="productId" class="mandatory">Savings instance name:</label>
		[@form.singleSelectWithPrompt "savingsAccountFormBean.productId" savingsAccountFormBean.productOfferingOptions "createSavingsAccount.selectProduct.selectPrompt" /]
	</div>
	</fieldset>
	<div class="row webflow-controls">
		<input type="submit" class="submit" value="Continue" name="_eventId_productSelected" />
		<input type="submit" class="cancel" value="Cancel" name="_eventId_cancel" />
	</div>
</form>

[/@layout.webflow]