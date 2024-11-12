import React, {useEffect, useState} from 'react';
import {ISalaryData, SalaryResponse} from "../types/ISalaryData";
import SalariesService from "../services/SalariesService";
import DataTable, {TableColumn} from "react-data-table-component";
import {Box, Button, Divider, Paper, Tooltip, Typography} from "@mui/material";
import {format} from "date-fns";
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import Swal from 'sweetalert2'
import AddSalaryDialog from "../components/AddSalaryDialog";
import EditSalaryDialog from "../components/EditSalaryDialog";

const SalariesScreen = () => {
    const [salaries, setSalaries] = useState<ISalaryData | null>(null);
    const [addDialogOpen, setAddDialogOpen] = useState(false);
    const [editDialogOpen, setEditDialogOpen] = useState(false);
    const [loading, setLoading] = useState(true);
    const [selectedSalary, setSelectedSalary] = useState<SalaryResponse | null>(null);

    useEffect(() => {
        setLoading(true);
        fetchSalaries();
    }, []);

    const fetchSalaries = () => {
        SalariesService.getAllSalaries()
            .then((response: any) => {
                setSalaries(response.data);
                setLoading(false);
            })
            .catch((e: Error) => {
                console.error(e);
                setLoading(false);
            })
    }

    const handleAdd = () => {
        setAddDialogOpen(true);
    }

    const handleAddDialogClose = () => {
        setAddDialogOpen(false);
        fetchSalaries();
    }
    const handleEdit = (row: SalaryResponse) => {
        setSelectedSalary(row);
        setEditDialogOpen(true);
    }

    const handleEditDialogClose = () => {
        setEditDialogOpen(false);
        setSelectedSalary(null);
        fetchSalaries();
    }

    const handleDelete = (row: SalaryResponse) => {
        Swal.fire({
            title: "Are you sure you want to delete this salary for employee " + row?.employee,
            text: "You won't be able to revert this!",
            icon: "warning",
            showCancelButton: true,
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
            confirmButtonText: "Yes, delete it!"
        }).then((result) => {
            if (result.isConfirmed) {
                deleteSalary(row?.salaryId)
            }
        });
    }

    const deleteSalary = (id: number) => {
        SalariesService.deleteSalary(id)
            .then((response: any) => {
                Swal.fire({
                    title: "Deleted!",
                    text: response.data,
                    icon: "success"
                }).then(r => {
                });

                fetchSalaries();
            })
            .catch((e: Error) => {
                Swal.fire({
                    icon: "error",
                    title: "Oops...",
                    text: "Something went wrong!"
                }).then(r => {
                });

                console.log(e);
            });
    }

    const createTooltipColumn = (
        name: string,
        selector: (row: SalaryResponse) => string
    ): TableColumn<SalaryResponse> => {
        return {
            name: <Typography variant="body1" color="primary">{name}</Typography>,
            cell: (row: SalaryResponse) => (
                <Tooltip title={selector(row)} arrow>
                    <Typography variant="body2" noWrap>
                        {selector(row)}
                    </Typography>
                </Tooltip>
            ),
            sortable: true,
        };
    };

    const columns: TableColumn<SalaryResponse>[] = [
        createTooltipColumn('Employee', (row) => row?.employee),
        {
            name: <Typography variant="body1" color="primary">Amount</Typography>,
            cell: (row: SalaryResponse) => (
                <Typography variant="body2" noWrap>
                    {row?.salary} EUR
                </Typography>
            ),
            sortable: true,
        },
        {
            name: <Typography variant="body1" color="primary">Date</Typography>,
            selector: row => row?.salaryDate,
            cell: (row: SalaryResponse) => (
                <Typography variant="caption">
                    {format(new Date(row?.salaryDate), 'PPP p')}
                </Typography>
            ),
            sortable: true,
        },
        {
            name: <Typography variant="body1" color="primary">Options</Typography>,
            cell: (row: SalaryResponse) => (
                <Box sx={{mt: 1, mb: 1, display: 'flex', flexDirection: 'column', gap: '8px'}}>
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={() => handleEdit(row)}
                        size="small"
                        startIcon={<EditIcon/>}
                    >
                        Edit
                    </Button>
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={() => handleDelete(row)}
                        size="small"
                        startIcon={<DeleteIcon/>}
                    >
                        Delete
                    </Button>
                </Box>
            ),
            style: {
                minWidth: '140px',
            },
        }
    ];

    if (loading) {
        return null;
    }

    return (
        <>

            <Paper elevation={3}
                   sx={{padding: 3, marginTop: 3, marginLeft: 3, marginRight: 3}}>

                <Box display="flex" justifyContent="flex-start" alignItems="center" mb={2}>
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={handleAdd}
                        size="small"
                        startIcon={<AddIcon/>}
                        sx={{mr: 2}}
                    >
                        Add
                    </Button>
                </Box>

                <DataTable
                    key={salaries?.data.length}
                    columns={columns}
                    data={salaries?.data || []}
                    pagination
                />
            </Paper>

            <AddSalaryDialog
                open={addDialogOpen}
                onClose={handleAddDialogClose}
            />

            <EditSalaryDialog
                open={editDialogOpen}
                onClose={handleEditDialogClose}
                rowData={selectedSalary}
            />
        </>
    );
};

export default SalariesScreen;