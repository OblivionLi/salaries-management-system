import axios from 'axios';
import {ISalaryCreateRequest} from "../types/ISalaryCreateRequest";


const getAllSalaries = () => {
    return axios.get(`/salaries/`, {
        headers: {
            'Content-Type': 'application/json'
        },
    });
}

const deleteSalary = (id : number) => {
    return axios.delete(`/salaries/delete/${id}`, {
        headers: {
            'Content-Type': 'application/json'
        },
    });
}

const addSalary = (data: ISalaryCreateRequest) => {
    return axios.post(`/salaries/add`, data, {
        headers: {
            'Content-Type': 'application/json'
        },
    });
}

const editSalary = (id: number, data: ISalaryCreateRequest) => {
    return axios.patch(`/salaries/edit/${id}`, data, {
        headers: {
            'Content-Type': 'application/json'
        },
    });
}

const SalariesScreen = {
    getAllSalaries,
    deleteSalary,
    addSalary,
    editSalary
}

export default SalariesScreen;