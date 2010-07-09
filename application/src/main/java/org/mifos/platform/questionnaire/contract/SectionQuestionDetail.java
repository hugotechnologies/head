/*
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
 */

package org.mifos.platform.questionnaire.contract;

public class SectionQuestionDetail {
    private int questionId;
    private boolean mandatory;
    private String title;

    public SectionQuestionDetail(int questionId, boolean mandatory) {
        this(questionId, null, mandatory);
    }

    public SectionQuestionDetail(int questionId, String title, boolean mandatory) {
        this.questionId = questionId;
        this.mandatory = mandatory;
        this.title = title;
    }

    public int getQuestionId() {
        return questionId;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SectionQuestionDetail that = (SectionQuestionDetail) o;

        if (questionId != that.questionId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return questionId;
    }
}