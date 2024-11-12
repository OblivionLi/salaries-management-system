import { SalaryResponse } from "./ISalaryData";

export interface ISalaryEditModalProps {
    open: boolean;
    onClose: () => void;
    rowData: SalaryResponse | null;
}