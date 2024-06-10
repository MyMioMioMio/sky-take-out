package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    private static final Integer DEFAULT_ADDRESS = 1;

    private static final Integer NOT_DEFAULT_ADDRESS = 0;

    @Override
    public List<AddressBook> list(AddressBook addressBook) {
        List<AddressBook> addressBookList = addressBookMapper.selectList(
                new LambdaQueryWrapper<AddressBook>()
                        .eq(addressBook.getId() != null, AddressBook::getId, addressBook.getId())
                        .eq(addressBook.getUserId() != null, AddressBook::getUserId, addressBook.getUserId())
                        .eq(addressBook.getConsignee() != null, AddressBook::getConsignee, addressBook.getConsignee())
                        .eq(addressBook.getPhone() != null, AddressBook::getPhone, addressBook.getPhone())
                        .eq(addressBook.getSex() != null, AddressBook::getSex, addressBook.getSex())
                        .eq(addressBook.getProvinceCode() != null, AddressBook::getProvinceCode, addressBook.getProvinceCode())
                        .eq(addressBook.getProvinceName() != null, AddressBook::getProvinceName, addressBook.getProvinceName())
                        .eq(addressBook.getCityCode() != null, AddressBook::getCityCode, addressBook.getCityCode())
                        .eq(addressBook.getCityName() != null, AddressBook::getCityName, addressBook.getCityName())
                        .eq(addressBook.getDistrictCode() != null, AddressBook::getDistrictCode, addressBook.getDistrictCode())
                        .eq(addressBook.getDistrictName() != null, AddressBook::getDistrictName, addressBook.getDistrictName())
                        .eq(addressBook.getDetail() != null, AddressBook::getDetail, addressBook.getDetail())
                        .eq(addressBook.getIsDefault() != null, AddressBook::getIsDefault, addressBook.getIsDefault())
                        .eq(addressBook.getLabel() != null, AddressBook::getLabel, addressBook.getLabel())
        );
        return addressBookList;
    }

    @Override
    public void addAddress(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.insert(addressBook);
    }

    @Override
    public void updateAddress(AddressBook addressBook) {
        addressBookMapper.updateById(addressBook);
    }

    @Override
    public void deleteAddressById(Long id) {
        addressBookMapper.deleteById(id);
    }

    @Override
    public void changeDefaultAddress(AddressBook addressBook) {
        //将本用户其他地址设置为非默认地址
        addressBookMapper.update(AddressBook.builder().isDefault(NOT_DEFAULT_ADDRESS).build(),
                new LambdaQueryWrapper<AddressBook>().eq(AddressBook::getUserId, BaseContext.getCurrentId()));
        //设置该地址为默认地址
        addressBook.setIsDefault(DEFAULT_ADDRESS);
        addressBookMapper.updateById(addressBook);
    }
}
