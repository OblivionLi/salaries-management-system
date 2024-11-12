import {Button, Dialog, DialogContent, DialogTitle, Paper, TextField} from '@mui/material';
import React, {useState} from 'react';
import IFinancialAddModalProps from "../types/IFinancialAddModalProps";
import {ISalaryCreateRequest} from "../types/ISalaryCreateRequest";
import Grid from '@mui/material/Grid2';
import SalariesService from "../services/SalariesService";

const AddSalaryDialog: React.FC<IFinancialAddModalProps> = ({open, onClose}) => {

    const [formData, setFormData] = useState<ISalaryCreateRequest>({
        employee: '',
        salary: '',
        salaryDate: new Date().toISOString().slice(0, 19) // value.slice trims the milliseconds from the date
    });

    const handleSubmit = (event: React.FormEvent) => {
        event.preventDefault();

        const salaryData: ISalaryCreateRequest = {
            ...formData,
        };

        addSalary(salaryData);
    };

    const addSalary = (data: ISalaryCreateRequest) => {
        SalariesService.addSalary(data)
            .then((response: any) => {
                onClose();
            })
            .catch((e: any) => {
                console.error(e);
            });
    }

    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = event.target;

        setFormData({
            ...formData,
            [name]: name === 'salaryDate' ? value.slice(0, 19) : value, // value.slice trims the milliseconds from the date
        });
    };

    const handleAmountChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = event.target;
        const regex = /^\d+(\.\d{0,2})?$/;
        if (value === '' || regex.test(value)) {
            setFormData({
                ...formData,
                [name]: value,
            });
        }
    };

    return (
        <Dialog open={open} onClose={onClose} maxWidth={"md"} fullWidth>
            <DialogTitle>Add Income</DialogTitle>
            <DialogContent>
                <Paper elevation={3}
                       sx={{padding: 3, marginTop: 3, width: '80%', marginLeft: 'auto', marginRight: 'auto'}}>
                    <form onSubmit={handleSubmit}>
                        <Grid container spacing={2}>
                            <Grid size={12}>
                                <TextField
                                    label="Employee"
                                    name="employee"
                                    value={formData.employee}
                                    onChange={handleChange}
                                    fullWidth
                                    required
                                />
                            </Grid>
                            <Grid size={12}>
                                <TextField
                                    label="Salary"
                                    name="salary"
                                    value={formData.salary}
                                    onChange={handleAmountChange}
                                    fullWidth
                                    required
                                />
                            </Grid>
                            <Grid size={12}>
                                <TextField
                                    label="Salary Date"
                                    name="salaryDate"
                                    value={formData.salaryDate}
                                    onChange={handleChange}
                                    type="datetime-local"
                                    fullWidth
                                    required
                                />
                            </Grid>
                            <Grid size={12}>
                                <Button type="submit" variant="contained" color="primary" fullWidth>
                                    Add Salary
                                </Button>
                            </Grid>
                        </Grid>
                    </form>
                </Paper>

            </DialogContent>
        </Dialog>
    );
};

export default AddSalaryDialog;