// Link Interface
export interface Link {
    rel: string; // Relationship (e.g., "self", "add", "edit")
    href: string; // URL for the link
    method: string; // HTTP method (e.g., "GET", "POST", "PATCH", "DELETE")
    version: string; // API version
}

// SalaryResponse Interface
export interface SalaryResponse {
    salaryId: number; // Salary id
    salary: number; // Salary value
    employee: string; // Employee name
    salaryDate: string; // Salary date in ISO 8601 format or null
    message: string | null; // Message, if any
    links: Link[]; // List of links related to the salary
}

// SalariesResponseWrapper Interface
export interface ISalaryData {
    data: SalaryResponse[]; // List of SalaryResponse objects
    links: Link[]; // List of links for pagination or other actions
}